package com.desertkun.brainout.events;

public class PlayStateUpdatedEvent extends Event
{
    @Override
    public ID getID()
    {
        return ID.playStateUpdated;
    }

    private Event init()
    {
        return this;
    }

    public static Event obtain()
    {
        PlayStateUpdatedEvent e = obtain(PlayStateUpdatedEvent.class);
        if (e == null) return null;
        return e.init();
    }

    @Override
    public void reset()
    {
    }
}
