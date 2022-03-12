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
import com.desertkun.brainout.utils.StringFunctions;

public class ConflictApprovePreviewMenu extends ConflictPreviewMenu
{
    private final Runnable approve;

    public ConflictApprovePreviewMenu(RoomSettings roomSettings, int conflictSize, Runnable approve)
    {
        super(roomSettings, conflictSize);

        this.approve = approve;
    }

    @Override
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

        TextButton connect = new TextButton(L.get("MENU_CLAN_CHALLENGE_ACCEPT"),
                BrainOutClient.Skin, "button-green");

        connect.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                pop();

                approve.run();
            }
        });
        buttons.add(connect).uniformX().expandX().fillX().height(64);
    }
}
