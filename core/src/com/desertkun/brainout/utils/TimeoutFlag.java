package com.desertkun.brainout.utils;

import com.badlogic.gdx.math.MathUtils;

import java.util.Date;

public class TimeoutFlag
{
    private long timer;

    public TimeoutFlag(float timeFor)
    {
        setValue(timeFor);
    }

    public float value()
    {
        return Math.max((float)(this.timer - System.currentTimeMillis()) / 1000.0f, 0.0f);
    }

    public boolean isGone()
    {
        return System.currentTimeMillis() > this.timer;
    }

    public void setValue(float value)
    {
        this.timer = System.currentTimeMillis() + (int)(value * 1000.0f);
    }
}
