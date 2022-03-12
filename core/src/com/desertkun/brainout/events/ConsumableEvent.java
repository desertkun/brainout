package com.desertkun.brainout.events;

import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class ConsumableEvent extends Event
{
    public enum Action
    {
        added,
        removed
    }

    public ConsumableRecord record;
    public Action action;

    @Override
    public ID getID()
    {
        return ID.consumable;
    }

    private Event init(ConsumableRecord record, Action action)
    {
        this.record = record;
        this.action = action;

        return this;
    }

    public static Event obtain(ConsumableRecord record, Action action)
    {
        ConsumableEvent e = obtain(ConsumableEvent.class);
        if (e == null) return null;
        return e.init(record, action);
    }

    @Override
    public void reset()
    {
        this.record = null;
        this.action = null;
    }
}
