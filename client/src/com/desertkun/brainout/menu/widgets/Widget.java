package com.desertkun.brainout.menu.widgets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;

public class Widget extends Table implements EventReceiver
{
    private Stage stage;

    public Widget()
    {
        setTouchable(Touchable.childrenOnly);

        addListener(new InputListener()
        {
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y)
            {
                return true;
            }
        });
    }

    public Widget(float x, float y, float w, float h)
    {
        this();

        setBounds(x, y, w, h);
    }

    public void setStage(Stage stage)
    {
        this.stage = stage;
    }

    @Override
    public Stage getStage()
    {
        return stage;
    }

    public void init()
    {
        //
    }

    public void release()
    {
        //
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
