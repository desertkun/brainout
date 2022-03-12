package com.desertkun.brainout.events;

import com.desertkun.brainout.data.interfaces.LaunchData;

public class InstrumentAnimationActionEvent extends Event
{
    public String action;
    public LaunchData customLaunchData;

    @Override
    public ID getID()
    {
        return ID.instrumentAnimationAction;
    }

    private Event init(String action, LaunchData customLaunchData)
    {
        this.action = action;
        this.customLaunchData = customLaunchData;

        return this;
    }

    public static Event obtain(String action)
    {
        InstrumentAnimationActionEvent e = obtain(InstrumentAnimationActionEvent.class);
        if (e == null) return null;
        return e.init(action, null);
    }

    public static Event obtain(String action, LaunchData customLaunchData)
    {
        InstrumentAnimationActionEvent e = obtain(InstrumentAnimationActionEvent.class);
        if (e == null) return null;
        return e.init(action, customLaunchData);
    }

    @Override
    public void reset()
    {
        action = null;
    }
}
