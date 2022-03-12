package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.online.RoomSettings;
import org.anthillplatform.runtime.services.GameService;

public class OtherPlayerPreviewMenu extends Menu
{
    private final GameService.PlayerRecord record;
    private final Runnable join;

    public OtherPlayerPreviewMenu(GameService.PlayerRecord record, Runnable join)
    {
        this.record = record;
        this.join = join;
    }

    @Override
    public Table createUI()
    {
        Table root = new Table(BrainOutClient.Skin);

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-gray");

            Label title = new Label(L.get("MENU_CONNECT_TO_SERVER"), BrainOutClient.Skin, "title-yellow");

            header.add(title).expandX().center();

            root.add(header).height(32).expandX().fillX().row();
        }

        Table data = new Table(BrainOutClient.Skin);
        data.setBackground("form-default");

        RoomSettings roomSettings = new RoomSettings();
        roomSettings.init(BrainOutClient.ClientController.getUserProfile(), false);
        roomSettings.read(record.getRoomSettings());

        if (roomSettings.getMap().isDefined())
        {
            Label title = new Label(L.get("MENU_MAP"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            data.add(title).width(192).pad(8).padTop(32);

            Label value = new Label(roomSettings.getMap().getValue(),
                    BrainOutClient.Skin, "title-small");
            value.setAlignment(Align.center);
            data.add(value).width(192).pad(8).padTop(32).row();
        }

        if (roomSettings.getMode().isDefined())
        {
            Label title = new Label(L.get("MENU_MODE"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            data.add(title).width(192).pad(8);

            Label value = new Label(roomSettings.getMode().getValue(),
                    BrainOutClient.Skin, "title-small");
            value.setAlignment(Align.center);
            data.add(value).width(192).pad(8).row();
        }

        {
            Label title = new Label(L.get("MENU_PLAYERS"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            data.add(title).width(192).pad(8);

            Label value = new Label(String.valueOf(record.getPlayers()) + " / " + record.getPlayersMax(),
                    BrainOutClient.Skin, "title-green");
            value.setAlignment(Align.center);
            data.add(value).width(192).pad(8).row();
        }

        if (roomSettings.getLevel() > 0)
        {
            Label title = new Label(L.get("MENU_PLAYER_LEVELS"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            data.add(title).width(192).pad(8).padBottom(32);

            Label value = new Label(String.valueOf(roomSettings.getLevel()),
                    BrainOutClient.Skin, "title-small");
            value.setAlignment(Align.center);
            data.add(value).width(192).pad(8).padBottom(32).row();
        }

        root.add(data).expandX().fillX().row();

        {
            Table buttons = new Table();

            {
                TextButton close = new TextButton(L.get("MENU_CLOSE"), BrainOutClient.Skin, "button-default");

                close.addListener(new ClickListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.back);

                        pop();
                    }
                });

                buttons.add(close).uniformX().expandX().fillX().height(64);
            }

            if (record.getPlayers() < record.getPlayersMax())
            {
                TextButton connect = new TextButton(L.get("MENU_CONNECT"), BrainOutClient.Skin, "button-green");

                connect.addListener(new ClickListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pop();

                        join.run();
                    }
                });

                buttons.add(connect).uniformX().expandX().fillX().height(64);
            }

            root.add(buttons).minWidth(384).expandX().fillX().colspan(2).row();
        }

        return root;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
