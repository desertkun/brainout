package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.menu.Menu;

public class FadeOutMenu extends Menu
{
    private final Color color;
    private final Runnable fadeOut;
    private float time;
    private final float maxTime;

    public FadeOutMenu(float time, Color color)
    {
        this(time, color, null);
    }

    public FadeOutMenu(float time, Color color, Runnable fadeOut)
    {
        this.time = time;
        this.maxTime = time;
        this.color = color;

        this.fadeOut = fadeOut;
    }

    @Override
    public void render()
    {
        float coef = MathUtils.clamp(time / maxTime, 0.f, 1.f);

        BrainOutClient.drawFade(coef, color, getBatch());
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        time -= delta;

        if (time < 0)
        {
            pop();

            if (fadeOut != null)
            {
                fadeOut.run();
            }
        }
    }

    @Override
    public Table createUI()
    {
        return null;
    }
}
