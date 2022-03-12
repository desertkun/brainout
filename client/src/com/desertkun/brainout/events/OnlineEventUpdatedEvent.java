package com.desertkun.brainout.events;

import com.desertkun.brainout.online.ClientEvent;

public class OnlineEventUpdatedEvent extends Event
{
    public ClientEvent event;

    @Override
    public ID getID()
    {
        return ID.onlineEventUpdated;
    }

    private Event init(ClientEvent event)
    {
        this.event = event;

        return this;
    }

    public static Event obtain(ClientEvent event)
    {
        OnlineEventUpdatedEvent e = obtain(OnlineEventUpdatedEvent.class);
        if (e == null) return null;
        return e.init(event);
    }

    @Override
    public void reset()
    {
        this.event = null;
    }
}
