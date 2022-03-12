package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class StepEvent extends Event
{
    public LaunchData launchData;
    public ActiveData who;

    @Override
    public ID getID()
    {
        return ID.step;
    }

    private Event init(LaunchData launchData, ActiveData who)
    {
        this.launchData = launchData;
        this.who = who;

        return this;
    }

    public static Event obtain(LaunchData launchData, ActiveData who)
    {
        StepEvent e = obtain(StepEvent.class);
        if (e == null) return null;
        return e.init(launchData, who);
    }

    @Override
    public void reset()
    {
        this.launchData = null;
        this.who = null;
    }
}
