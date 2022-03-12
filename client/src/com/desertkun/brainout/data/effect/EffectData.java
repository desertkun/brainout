package com.desertkun.brainout.data.effect;

import com.desertkun.brainout.content.effect.Effect;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.Renderable;
import com.desertkun.brainout.data.interfaces.CompleteUpdatable;

public abstract class EffectData implements Renderable, CompleteUpdatable
{
    private final Effect effect;
    private final LaunchData launchData;
    private boolean done;
    private float angleOffset;

    public EffectData(Effect effect, LaunchData launchData)
    {
        this.effect = effect;
        this.done = false;
        this.launchData = launchData;
        this.angleOffset = 0;
    }

    public LaunchData getLaunchData()
    {
        return launchData;
    }

    public Effect getEffect()
    {
        return effect;
    }

    public int getEffectLayer() { return 0; }

    public float getX()
    {
        return launchData.getX();
    }

    public float getY()
    {
        return launchData.getY();
    }

    public void setAngleOffset(float angleOffset)
    {
        this.angleOffset = angleOffset;
    }

    public float getAngle()
    {
        return launchData.getAngle() + angleOffset;
    }

    public Map getMap()
    {
        return Map.Get(launchData.getDimension());
    }

    public String getDimension()
    {
        return launchData.getDimension();
    }

    @Override
    public void release()
    {
        this.done = true;
    }

    public boolean isDone()
    {
        return done;
    }

    @Override
    public int getZIndex()
    {
        return 0;
    }

    @Override
    public int getLayer()
    {
        return 0;
    }
}
