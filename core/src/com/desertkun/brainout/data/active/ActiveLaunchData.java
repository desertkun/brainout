package com.desertkun.brainout.data.active;

import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.ActiveLaunchData")
public class ActiveLaunchData extends LaunchData
{
    public ActiveData activeData;

    public ActiveLaunchData(ActiveData activeData)
    {
        super();

        this.activeData = activeData;
    }

    @Override
    public float getX()
    {
        return activeData.getX();
    }

    @Override
    public float getY()
    {
        return activeData.getY();
    }

    @Override
    public float getAngle()
    {
        return activeData.getAngle();
    }

    @Override
    public String getDimension()
    {
        return activeData.getDimension();
    }

    @Override
    public boolean getFlipX()
    {
        return false;
    }
}
