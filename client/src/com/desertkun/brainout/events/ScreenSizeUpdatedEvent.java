package com.desertkun.brainout.events;

public class ScreenSizeUpdatedEvent extends Event
{
    @Override
    public ID getID()
    {
        return ID.screenSizeUpdated;
    }

    private Event init()
    {
        return this;
    }

    public static Event obtain()
    {
        ScreenSizeUpdatedEvent e = obtain(ScreenSizeUpdatedEvent.class);
        if (e == null) return null;
        return e.init();
    }

    @Override
    public void reset()
    {
    }
}
