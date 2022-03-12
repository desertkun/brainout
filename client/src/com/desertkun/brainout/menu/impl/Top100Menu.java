package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.LeaderboardList;
import com.desertkun.brainout.menu.ui.MenuHelper;
import org.anthillplatform.runtime.services.LoginService;

public class Top100Menu extends Menu
{
    private final LeaderboardList leaderboard;
    private boolean onTop;

    public Top100Menu()
    {
        onTop = true;

        leaderboard = new LeaderboardList("top100", "desc", 3,
            BrainOutClient.ClientController.getMyAccount(), false)
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
                GameState gs = getGameState();

                if (gs == null)
                    return;

                close();

                gs.pushMenu(new RemoteAccountMenu(accountId, credential));
            }
        };

    }

    @Override
    public void onInit()
    {
        super.onInit();

        MenuHelper.AddCloseButton(this, this::close);
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        {
            Label top100 = new Label(L.get("MENU_TOP_100"), BrainOutClient.Skin, "title-small");
            data.add(top100).padTop(16).row();
        }
        {
            Label top100 = new Label(L.get("MENU_TOP_100_BY_EFFICIENCY"), BrainOutClient.Skin, "title-yellow");
            data.add(top100).padBottom(16).row();
        }
        {
            Label description = new Label(L.get("MENU_TOP_100_EFFICIENCY_DESC"), BrainOutClient.Skin, "title-small");
            description.setAlignment(Align.center);
            data.add(description).pad(16).row();
        }

        data.add(leaderboard).width(580).expandY().fillY().padTop(32).padBottom(32).row();

        setScrollFocus(leaderboard);

        return data;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public boolean stayOnTop()
    {
        return onTop;
    }

    private void close()
    {
        onTop = false;
        pop();
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
