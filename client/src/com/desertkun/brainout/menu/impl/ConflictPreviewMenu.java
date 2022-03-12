package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.online.RoomSettings;

public class ConflictPreviewMenu extends Menu
{
    private RoomSettings roomSettings;
    private int conflictSize;

    public ConflictPreviewMenu(RoomSettings roomSettings, int conflictSize)
    {
        this.roomSettings = roomSettings;
        this.conflictSize = conflictSize;
    }

    @Override
    public Table createUI()
    {
        Table root = new Table(BrainOutClient.Skin);

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label title = new Label(L.get("MENU_CLAN_CHALLENGED"), BrainOutClient.Skin, "title-small");

            header.add(title).expandX().center();

            root.add(header).height(32).expandX().fillX().row();
        }

        Table data = new Table(BrainOutClient.Skin);
        data.setBackground("form-border-red");

        data.add().pad(16).row();

        if (roomSettings.getMap().isDefined())
        {
            Label title = new Label(L.get("MENU_MAP"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            data.add(title).width(192).pad(8);

            Label value = new Label(roomSettings.getMap().getValue(),
                    BrainOutClient.Skin, "title-small");
            value.setAlignment(Align.center);
            data.add(value).width(192).pad(8).row();
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

        if (roomSettings.getDisableBuilding().isDefined())
        {
            Label title = new Label(L.get("MENU_DISABLE_BUILDING"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            data.add(title).width(192).pad(8);

            Label value = new Label(
                    roomSettings.getDisableBuilding().getValue() ? L.get("MENU_YES") : L.get("MENU_NO"),
                    BrainOutClient.Skin, "title-small");
            value.setAlignment(Align.center);
            data.add(value).width(192).pad(8).row();
        }

        if (roomSettings.getPreset() != null)
        {
            String v = ClientConstants.Presets.PRESETS.get(roomSettings.getPreset());

            if (v != null)
            {
                Label title = new Label(L.get("MENU_PRESET"), BrainOutClient.Skin, "title-yellow");
                title.setAlignment(Align.center);
                data.add(title).width(192).pad(8);

                Label value = new Label(L.get(v), BrainOutClient.Skin, "title-small");
                value.setAlignment(Align.center);
                data.add(value).width(192).pad(8).row();
            }
        }

        {
            Label title = new Label(L.get("MENU_CLAN_CHALLENGE_SIZE"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            data.add(title).width(192).pad(8);

            int half = conflictSize / 2;

            Label value = new Label(String.valueOf(half) + "x" + String.valueOf(half),
                    BrainOutClient.Skin, "title-green");
            value.setAlignment(Align.center);
            data.add(value).width(192).pad(8).row();
        }

        data.add().pad(16).row();

        root.add(data).expandX().fillX().row();

        {
            Table buttons = new Table();
            renderButtons(buttons);
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

    protected void renderButtons(Table buttons)
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

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
