package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.PlayerProfileMenu;

public class MenuHelper
{
    public static void AddCloseButton(Menu menu, Runnable callback)
    {
        TextButton close = new TextButton(L.get("MENU_CLOSE"),
                BrainOutClient.Skin, "button-default");

        close.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(Menu.MenuSound.back);

                callback.run();
            }
        });

        close.setBounds(BrainOutClient.getWidth() - 212, BrainOutClient.getHeight() - 84, 192, 64);
        menu.addActor(close);
    }

    public static void AddSingleButton(Menu menu, String text, Runnable callback)
    {
        TextButton close = new TextButton(text, BrainOutClient.Skin, "button-default");

        close.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                callback.run();
            }
        });

        close.setBounds(BrainOutClient.getWidth() - 212, BrainOutClient.getHeight() - 84, 192, 64);
        menu.addActor(close);
    }

    public static Table AddButtonsContainers(Menu menu)
    {
        Table container = new Table();
        container.setTouchable(Touchable.childrenOnly);
        container.align(Align.top);

        container.setBounds(BrainOutClient.getWidth() - 212, BrainOutClient.getHeight() - 484, 192, 464);
        menu.addActor(container);

        return container;
    }

    public static Table AddLeftButtonsContainers(Menu menu)
    {
        Table container = new Table();
        container.setTouchable(Touchable.childrenOnly);
        container.align(Align.top | Align.left);

        container.setBounds(16, BrainOutClient.getHeight() - 484, 400, 464);
        menu.addActor(container);

        return container;
    }
}
