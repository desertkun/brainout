package com.desertkun.brainout.events;

import com.desertkun.brainout.data.interfaces.LaunchData;

public class LaunchEffectEvent extends Event
{
    public enum Kind
    {
        hit,
        destroy,
        step,
        shoot,
        reload,
        fetch,
        place,
        switchMode,
        bleeding,
        custom,
        buildUp
    }

    public Kind kind;
    public LaunchData launchData;
    public String custom;

    @Override
    public ID getID()
    {
        return ID.launchEffect;
    }

    private Event init(Kind kind, LaunchData launchData)
    {
        this.kind = kind;
        this.launchData = launchData;

        return this;
    }

    private Event init(Kind kind, LaunchData launchData, String custom)
    {
        this.kind = kind;
        this.launchData = launchData;
        this.custom = custom;

        return this;
    }

    public static Event obtain(Kind kind, LaunchData launchData)
    {
        LaunchEffectEvent e = obtain(LaunchEffectEvent.class);
        if (e == null) return null;
        return e.init(kind, launchData);
    }

    public static Event obtain(Kind kind, LaunchData launchData, String custom)
    {
        LaunchEffectEvent e = obtain(LaunchEffectEvent.class);
        if (e == null) return null;
        return e.init(kind, launchData, custom);
    }

    @Override
    public void reset()
    {
        this.kind = null;
        this.launchData = null;
        this.custom = null;
    }
}
