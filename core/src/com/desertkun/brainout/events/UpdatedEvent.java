package com.desertkun.brainout.events;

public class UpdatedEvent extends Event
{
    @Override
    public ID getID()
    {
        return ID.updated;
    }

    private Event init()
    {
        return this;
    }

    public static Event obtain()
    {
        UpdatedEvent e = obtain(UpdatedEvent.class);
        if (e == null) return null;
        return e.init();
    }

    @Override
    public void reset()
    {
    }
}
