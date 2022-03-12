package com.desertkun.brainout.events;

import com.desertkun.brainout.common.enums.EntityReceived;

public class ActiveReceivedContentEvent extends Event
{
    public EntityReceived entity;
    public int amount;

    @Override
    public ID getID()
    {
        return ID.activeContentReceivedNotice;
    }

    private Event init(EntityReceived entity, int amount)
    {
        this.entity = entity;
        this.amount = amount;

        return this;
    }

    public static Event obtain(EntityReceived entity, int amount)
    {
        ActiveReceivedContentEvent e = obtain(ActiveReceivedContentEvent.class);
        if (e == null) return null;
        return e.init(entity, amount);
    }

    @Override
    public void reset()
    {
        this.entity = null;
        this.amount = 0;
    }
}
