package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.desertkun.brainout.menu.Menu;

public class ClickOverListener extends ClickListener
{
    public ClickOverListener() {}
    public ClickOverListener(int button)
    {
        super(button);
    }
    @Override
    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
    {
        if (!isOver())
        {
           Menu.playSound(Menu.MenuSound.hover);
        }

        super.enter(event, x, y, pointer, fromActor);
    }
}
