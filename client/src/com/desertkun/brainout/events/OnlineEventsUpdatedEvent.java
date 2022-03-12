package com.desertkun.brainout.events;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.online.ClientEvent;

public class OnlineEventsUpdatedEvent extends Event
{
    public Array<ClientEvent> events;

    @Override
    public ID getID()
    {
        return ID.onlineEventsUpdated;
    }

    private Event init(Array<ClientEvent> events)
    {
        this.events = events;

        return this;
    }

    public static Event obtain(Array<ClientEvent> events)
    {
        OnlineEventsUpdatedEvent e = obtain(OnlineEventsUpdatedEvent.class);
        if (e == null) return null;
        return e.init(events);
    }

    @Override
    public void reset()
    {
        this.events = null;
    }
}
