package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.ForceTopMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.LeaderboardList;
import com.desertkun.brainout.menu.ui.MenuHelper;
import com.desertkun.brainout.online.UserProfile;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.SocialService;

import java.util.ArrayList;
import java.util.List;

public class BrowseClansMenu extends ForceTopMenu
{
    private Table clanList;
    private String filter;
    private EventListener searchItemClicked;

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Label top100 = new Label(L.get("MENU_TOP_100"), BrainOutClient.Skin, "title-small");
            data.add(top100).padTop(128).row();
        }
        {
            Label top100 = new Label(L.get("MENU_TOP_100_CLANS"), BrainOutClient.Skin, "title-yellow");
            data.add(top100).padBottom(16).row();
        }

        {
            Table search = new Table(BrainOutClient.Skin);
            search.setBackground("border-dark-blue");

            Table filter = new Table();

            TextField filterText = new TextField("", BrainOutClient.Skin, "edit-default");
            filterText.addListener(new ChangeListener()
            {
                @Override
                public void changed(ChangeEvent event, Actor actor)
                {
                    BrowseClansMenu.this.filter = filterText.getText().toLowerCase();

                    filterText.clearActions();
                    filterText.addAction(Actions.sequence(
                        Actions.delay(0.5f),
                        Actions.run(BrowseClansMenu.this::updateClanList)
                    ));
                }
            });
            setKeyboardFocus(filterText);

            Image searchIcon = new Image(BrainOutClient.getRegion("icon-search"));

            filter.add(filterText).expandX().fillX();
            filter.add(searchIcon).padLeft(-36);

            search.add(filter).expandX().fillX().padLeft(64).padRight(64).row();

            data.add(search).width(600).fillX().height(64).row();
        }

        clanList = new Table();
        data.add(clanList).width(600).expandY().top().row();

        renderDefaultClanList();

        return data;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        if (BrainOutClient.ClientController.isLobby())
        {

            Table leftButtons = MenuHelper.AddLeftButtonsContainers(this);

            // create a clan

            if (BrainOut.OnlineEnabled() && userProfile != null && !userProfile.isParticipatingClan())
            {
                Button btn = new Button(BrainOutClient.Skin, "button-green");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        GameState gs = getGameState();

                        if (gs == null)
                            return;

                        close();

                        gs.pushMenu(new CreateClanMenu());
                    }
                });

                Image image = new Image(BrainOutClient.getRegion("skillpoints-big"));
                image.setScaling(Scaling.none);
                btn.add(image).expand().fill();

                Label title = new Label(L.get("MENU_CREATE_CLAN"), BrainOutClient.Skin, "title-small");
                title.setAlignment(Align.center);

                leftButtons.add(btn).size(64, 64).padRight(10);
                leftButtons.add(title).left().row();
            }
        }

        MenuHelper.AddCloseButton(this, this::close);

        this.searchItemClicked = new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                Actor actor = event.getTarget();

                if (actor == null)
                    return;

                Object userObject = actor.getUserObject();

                if (!(userObject instanceof SocialService.Group))
                    return;

                clanSelected(((SocialService.Group) userObject).getId());
            }
        };
    }

    private void clanSelected(String clanId)
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        close();

        gs.pushMenu(new ClanMenu(clanId));
    }

    private void updateClanList()
    {
        if (filter == null || filter.isEmpty())
        {
            renderDefaultClanList();
        }
        else
        {
            renderClanSearch();
        }
    }

    private void renderWait()
    {
        clanList.clear();

        Label wait = new Label(L.get("MENU_PLEASE_WAIT"), BrainOutClient.Skin, "title-gray");
        wait.setAlignment(Align.center);

        clanList.add(wait).pad(32).expandX().fillX().center().row();
    }

    private void renderClanSearch()
    {
        renderWait();

        SocialService socialService = SocialService.Get();
        LoginService loginService = LoginService.Get();

        if (socialService != null && loginService != null)
        {
            if (this.filter.matches("^-?\\d+$"))
            {
                socialService.getGroup(loginService.getCurrentAccessToken(), this.filter,
                    (service, request, result, group) -> Gdx.app.postRunnable(() ->
                {
                    if (result == Request.Result.success)
                    {
                        ArrayList<SocialService.Group> groups = new ArrayList<>();
                        groups.add(group);

                        renderFoundGroups(groups);
                    }
                    else
                    {
                        renderError(L.get("MENU_ONLINE_ERvROR", result.toString()));
                    }
                }));

                return;
            }

            socialService.searchGroups(loginService.getCurrentAccessToken(), this.filter,
                (service, request, result, groups) -> Gdx.app.postRunnable(() ->
            {
                if (result == Request.Result.success)
                {
                    renderFoundGroups(groups);
                }
                else
                {
                    renderError(L.get("MENU_ONLINE_ERROR", result.toString()));
                }
            }));
        }
    }

    private void renderError(String errorText)
    {
        clanList.clear();

        Label wait = new Label(errorText, BrainOutClient.Skin, "title-red");
        wait.setAlignment(Align.center);

        clanList.add(wait).pad(32).expandX().fillX().center().row();
    }

    private void renderFoundGroups(List<SocialService.Group> groups)
    {
        if (groups.isEmpty())
        {
            renderNoResults();
            return;
        }

        clanList.clear();

        int i = 0;
        for (SocialService.Group group : groups)
        {
            String style;

            if (i % 2 == 0)
            {
                style = "button-row-dark-blue";
            }
            else
            {
                style = "button-row-border-blue";
            }

            String labelStyle = "title-small";


            Button btn = new Button(BrainOutClient.Skin, style);

            String avatar = group.getProfile() != null ?
                                group.getProfile().optString("avatar", null)
                            : null;

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
                });

                btn.add(image).size(48, 48).padRight(8).padLeft(4);
            }
            else
            {
                btn.add().size(48, 48).padRight(8).padLeft(4);
            }

            Label name = new Label(group.getName(),
                    BrainOutClient.Skin, labelStyle);
            name.setTouchable(Touchable.disabled);
            btn.add(name).expandX().left().padRight(8);

            btn.setUserObject(group);

            btn.addListener(this.searchItemClicked);

            clanList.add(btn).expandX().fillX().row();

            i++;
        }
    }

    private void renderNoResults()
    {
        clanList.clear();

        Label wait = new Label(L.get("MENU_NO_RESULTS"), BrainOutClient.Skin, "title-small");
        wait.setAlignment(Align.center);

        clanList.add(wait).pad(32).expandX().fillX().center().row();
    }

    private void renderDefaultClanList()
    {
        clanList.clear();

        String myClan = null;

        if (BrainOutClient.SocialController.getMyClan() != null)
        {
            myClan = BrainOutClient.SocialController.getMyClan().getId();
        }

        LeaderboardList leaderboardList = new LeaderboardList("clans100", "desc", 3, 100, myClan, true)
        {
            @Override
            protected String getStyleEven()
            {
                return "button-row-dark-blue";
            }

            @Override
            protected String getStyleOdd()
            {
                return "button-row-border-blue";
            }

            @Override
            protected void clickedOnItem(String accountId, String credential)
            {
                clanSelected(accountId);
            }
        };

        leaderboardList.setOverscroll(false, false);
        setScrollFocus(leaderboardList);

        clanList.add(leaderboardList).expand().fill().row();
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
