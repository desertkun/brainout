package com.desertkun.brainout.events;

public class BadgeReadEvent extends Event
{
    @Override
    public ID getID()
    {
        return ID.badgeRead;
    }

    private Event init()
    {
        return this;
    }

    public static Event obtain()
    {
        BadgeReadEvent e = obtain(BadgeReadEvent.class);
        if (e == null) return null;
        return e.init();
    }

    @Override
    public void reset()
    {
    }
}
