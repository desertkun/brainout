package com.desertkun.brainout.menu.tutorial;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.menu.Menu;

public class TutorialMenu extends Menu
{
    private Runnable closed;

    public TutorialMenu(Runnable closed)
    {
        this.closed = closed;
    }

    @Override
    public void render()
    {
        drawFade();

        super.render();
    }

    protected void drawFade()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());
    }

    @Override
    public boolean lockUpdate()
    {
        return true;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                close();
            }
        });
    }

    protected void close()
    {
        if (closed != null)
        {
            pop();
            closed.run();

            closed = null;
        }
    }

    @Override
    public Table createUI()
    {
        return null;
    }
}
