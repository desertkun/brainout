package com.desertkun.brainout.data.interfaces;

public abstract class LaunchData implements Animable
{
    public LaunchData()
    {
    }

    public abstract float getX();
    public abstract float getY();
    public abstract float getAngle();
    public abstract String getDimension();
}
