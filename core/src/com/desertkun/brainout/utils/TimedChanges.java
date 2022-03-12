package com.desertkun.brainout.utils;

import com.desertkun.brainout.Constants;
import com.esotericsoftware.minlog.Log;

public abstract class TimedChanges
{
    private final float timeDetect;
    private final String name;
    private float lastTime;
    private boolean forceUpdate;

    public TimedChanges(String name)
    {
        this(name, Constants.Moves.CHANGE_TIME_DEFAULT);
    }

    public TimedChanges(String name, float timeDetect)
    {
        this.lastTime = 0;
        this.timeDetect = timeDetect;
        this.name = name;
        this.forceUpdate = false;
    }

    public boolean update(float dt)
    {
        boolean result = false;
        boolean forceUpdate = isForceUpdate();

        if (lastTime > 0 && !forceUpdate)
        {
            lastTime -= dt;
        }
        else
        {
            if (forceUpdate || changesMade())
            {
                reset(this);
                sendChanges();

                result = true;
            }

            lastTime = timeDetect;
        }

        return result;
    }

    public boolean canBeResetByOthers()
    {
        return true;
    }

    public abstract void reset(TimedChanges who);
    public abstract boolean changesMade();
    public abstract void sendChanges();

    public boolean isForceUpdate()
    {
        return false;
    }
}
