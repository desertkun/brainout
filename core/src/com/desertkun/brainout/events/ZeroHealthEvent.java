package com.desertkun.brainout.events;

public class ZeroHealthEvent extends Event
{
    @Override
    public ID getID()
    {
        return ID.onZeroHealth;
    }

    private Event init()
    {
        return this;
    }

    public static Event obtain()
    {
        ZeroHealthEvent e = obtain(ZeroHealthEvent.class);
        if (e == null) return null;
        return e.init();
    }

    @Override
    public void reset()
    {
    }
}
