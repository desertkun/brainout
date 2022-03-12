package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.data.interfaces.SelectableItem;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import org.json.JSONObject;

public class SelectTeamMenu extends FormMenu
{
    public abstract static class Select
    {
        public abstract void onSelect(Team item, GameState gs);
        public abstract void failed(String reason);
    }

    private final Array<Team> teams;
    private final Select select;

    public SelectTeamMenu(Array<Team> teams, Select select)
    {
        this.teams = teams;
        this.select = select;
    }

    @Override
    public Table createUI()
    {
        Table data = super.createUI();

        data.add(new Label(L.get("MENU_SELECT_TEAM"),
            BrainOutClient.Skin, "title-medium")).pad(16).row();

        Table items = new Table();

        for (final Team item: this.teams)
        {
            Table cnt = new Table();

            SelectableItem icon = item.getComponent(IconComponent.class);

            TextButton btn = new TextButton(icon.getItemName(), BrainOutClient.Skin, "button-small");
            btn.setClip(true);

            TextureAtlas.AtlasRegion region = icon.getItemIcon();

            if (region != null)
            {
                cnt.add(new Image(region)).pad(10).center().row();
            }
            cnt.add(btn).expandX().fillX().pad(5).row();

            items.add(cnt).expandX().fillX().pad(10);

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    GameState gs = getGameState();

                    if (gs == null)
                        return;

                    playSound(MenuSound.select);

                    teamSelected(item, gs);
                }
            });
        }

        data.add(items).pad(16).expandX().fillX().row();

        TextButton btn = new TextButton(L.get("MENU_CANCEL"),
            BrainOutClient.Skin, "button-small");

        btn.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                GameState gs = getGameState();

                if (gs == null)
                    return;

                pop();
            }
        });

        data.add(btn).width(128).pad(16).expandX().center().pad(4).row();

        return data;
    }

    private void teamSelected(Team team, GameState gs)
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");

        gs.pushMenu(waitLoadingMenu);

        final CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);

        csGame.changeTeam(team, new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                gs.popMenu(SelectTeamMenu.this);
                select.onSelect(team, gs);

            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                gs.popMenu(SelectTeamMenu.this);
                select.failed(reason);
            }
        });
    }
}
