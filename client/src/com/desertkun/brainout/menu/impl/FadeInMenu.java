package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.menu.Menu;

public class FadeInMenu extends Menu
{
    private final Runnable faded;
    private Runnable fadeOut;
    private final Color color;
    private float time, maxTime;

    public FadeInMenu(float time, Color color, Runnable faded)
    {
        this.time = time;
        this.maxTime = time;
        this.faded = faded;
        this.color = color;
    }

    public FadeInMenu(float time, Runnable faded)
    {
        this(time, Color.BLACK, faded);
    }

    public FadeInMenu(float time, Runnable faded, Runnable fadeOut)
    {
        this(time, Color.BLACK, faded);

        this.fadeOut = fadeOut;
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        if (time > 0)
        {
            time -= delta;

            if (time <= 0)
            {
                done();
            }
        }
    }

    protected void done()
    {
        pop();

        if (faded != null)
        {
            faded.run();
        }

        BrainOutClient.getInstance().topState().pushMenu(new FadeOutMenu(maxTime, color, fadeOut));
    }

    @Override
    public void render()
    {
        float coef = 1.0f - MathUtils.clamp(time / maxTime, 0.f, 1.f);

        BrainOutClient.drawFade(coef, color, getBatch());
    }

    @Override
    public Table createUI()
    {
        return null;
    }
}
