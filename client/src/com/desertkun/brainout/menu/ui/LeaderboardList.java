package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.utils.StringFunctions;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LeaderboardService;
import org.anthillplatform.runtime.services.LoginService;
import org.json.JSONObject;

public class LeaderboardList extends ScrollPane
{
    private final String leaderboardName;
    private final String order;
    private final Table container;
    private final int goldNumbers;
    private final int limit;
    private final ClickOverListener clicked;
    private final String myAccount;
    private final boolean arbitrary;

    public LeaderboardList(String leaderboardName, String order, int goldNumbers, String myAccount, boolean arbitrary)
    {
        this(leaderboardName, order, goldNumbers, 100, myAccount, arbitrary);
    }

    public LeaderboardList(String leaderboardName, String order, int goldNumbers, int limit, String myAccount,
                           boolean arbitrary)
    {
        super(new Table(), BrainOutClient.Skin, "scroll-default");

        setScrollingDisabled(true, false);
        setFadeScrollBars(false);

        this.leaderboardName = leaderboardName;
        this.order = order;
        this.container = ((Table) getWidget());
        this.goldNumbers = goldNumbers;
        this.limit = limit;

        this.clicked = new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Actor actor = event.getTarget();

                if (actor == null)
                    return;

                Object userObject = actor.getUserObject();

                if (!(userObject instanceof LeaderboardService.LeaderboardResult.Entry))
                    return;

                Menu.playSound(Menu.MenuSound.select);

                LeaderboardService.LeaderboardResult.Entry entry =
                    ((LeaderboardService.LeaderboardResult.Entry) userObject);

                clickedOnItem(entry.account, entry.profile.optString("credential", ""));
            }
        };

        this.myAccount = myAccount;
        this.arbitrary = arbitrary;

        init();

        loading();
    }

    private void init()
    {
        if (!BrainOut.OnlineEnabled())
        {
            Gdx.app.postRunnable(() ->
            {
                LeaderboardService.LeaderboardResult result = new LeaderboardService.LeaderboardResult();

                LeaderboardService.LeaderboardResult.Entry entry = new LeaderboardService.LeaderboardResult.Entry();
                entry.account = "1";
                entry.profile = new JSONObject("{}");
                entry.display_name = "Test Clan";
                entry.rank = 1;
                entry.score = 10;
                result.getEntries().add(entry);

                renderResult(result);
            });

            return;
        }

        LeaderboardService leaderboardService = LeaderboardService.Get();
        LoginService loginService = LoginService.Get();

        if (leaderboardService == null)
            throw new RuntimeException("No leaderboard service!");

        if (loginService == null)
            throw new RuntimeException("No login service!");


        proceed(leaderboardService, loginService);
    }

    private void proceed(LeaderboardService leaderboardService, LoginService loginService)
    {
        LoginService.AccessToken token = loginService.getCurrentAccessToken();

        leaderboardService.getLeaderboard(token, leaderboardName, order, limit, 0, arbitrary ? myAccount : null,
            (service1, request, result, data) ->
        {
            Gdx.app.postRunnable(() ->
            {
                if (result == Request.Result.success)
                {
                    renderResult(data);
                }
                else
                {
                    renderError(result);
                }
            });

        });
    }

    private void loading()
    {
        container.clear();

        Label text = new Label(
                L.get("MENU_LOADING"), BrainOutClient.Skin, "title-gray");
        text.setAlignment(Align.center, Align.center);

        container.add(text).expand().fill().row();
    }

    public Table getContainer()
    {
        return container;
    }

    protected String getStyleEven()
    {
        return "button-row-gray";
    }

    protected String getStyleOdd()
    {
        return "button-row-default";
    }

    protected void clickedOnItem(String accountId, String credential)
    {

    }

    private void renderResult(LeaderboardService.LeaderboardResult result)
    {
        container.clear();
        container.align(Align.top);

        int i = 0;

        for (LeaderboardService.LeaderboardResult.Entry entry : result.getEntries())
        {
            boolean gold = false;

            String style;

            if (i == 0)
            {
                style = "button-row-red";
                gold = true;
            }
            else
            {
                if (i % 2 == 0)
                {
                    style = getStyleEven();
                }
                else
                {
                    style = getStyleOdd();
                }
            }

            String labelStyle;

            if (myAccount != null && myAccount.equals(entry.account))
            {
                labelStyle = "title-green";
            }
            else if (i < goldNumbers)
            {
                labelStyle = "title-yellow";
            }
            else
            {
                labelStyle = "title-small";
            }

            Button btn = new Button(BrainOutClient.Skin, style);

            String avatar = entry.profile.optString("avatar", null);

            if (avatar != null)
            {
                Image image = new Image();
                image.setTouchable(Touchable.disabled);

                Avatars.Get(avatar, (has, avatar1) ->
                {
                    if (has)
                    {
                        image.setDrawable(new TextureRegionDrawable(new TextureRegion(avatar1)));
                    }
                    else
                    {
                        image.setDrawable(BrainOutClient.Skin, "default-avatar");
                    }
                });

                btn.add(image).size(48, 48).padRight(8).padLeft(4);
            }
            else
            {
                Image image = new Image(BrainOutClient.Skin, "default-avatar");
                btn.add(image).size(48, 48).padRight(8).padLeft(4);
            }

            Label rank = new Label(String.valueOf(entry.rank),
                BrainOutClient.Skin, labelStyle);
            rank.setTouchable(Touchable.disabled);
            btn.add(rank).padRight(8);

            Label name = new Label(entry.display_name,
                BrainOutClient.Skin, labelStyle);
            name.setTouchable(Touchable.disabled);
            btn.add(name).padRight(8);

            Label score = new Label(StringFunctions.format(getScore(entry.account, entry.score)),
                BrainOutClient.Skin, labelStyle);
            score.setTouchable(Touchable.disabled);
            btn.add(score).padRight(8).expandX().right();

            btn.setUserObject(entry);

            btn.addListener(this.clicked);

            container.add(btn).expandX().fillX().row();

            i++;
        }
    }

    public float getScore(String account, float score)
    {
        return score;
    }

    private void renderNodata()
    {
        container.clear();

        Label text = new Label(
                L.get("MENU_NO_INFORMATION_YET"), BrainOutClient.Skin, "title-gray");
        text.setAlignment(Align.center, Align.center);

        container.add(text).expand().fill().row();
    }

    private void renderError(Request.Result status)
    {
        if (status == Request.Result.notFound)
        {
            renderNodata();

            return;
        }

        container.clear();

        Label text = new Label(status.toString(), BrainOutClient.Skin, "title-medium-red");
        text.setAlignment(Align.center, Align.center);

        container.add(text).expand().fill().row();
    }

}
