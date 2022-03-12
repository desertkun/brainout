package com.desertkun.brainout.data.interfaces;

import com.desertkun.brainout.data.Map;

public class StepPointData extends LaunchData
{
    private final float v;
    private final float p;
    private final String d;

    public StepPointData(float v, float p, int d)
    {
        this.v = v;
        this.p = p;
        this.d = Map.FindDimension(d);
    }

    @Override
    public float getX()
    {
        return v;
    }

    @Override
    public float getY()
    {
        return p;
    }

    @Override
    public float getAngle()
    {
        return 0;
    }

    @Override
    public boolean getFlipX()
    {
        return false;
    }

    @Override
    public String getDimension()
    {
        return d;
    }
}
