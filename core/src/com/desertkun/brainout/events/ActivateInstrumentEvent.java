package com.desertkun.brainout.events;

import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class ActivateInstrumentEvent extends Event
{
    public ConsumableRecord record;

    private Event init(ConsumableRecord record)
    {
        this.record = record;

        return this;
    }

    public static Event obtain(ConsumableRecord record)
    {
        ActivateInstrumentEvent e = obtain(ActivateInstrumentEvent.class);
        if (e == null) return null;
        return e.init(record);
    }

    @Override
    public ID getID()
    {
        return ID.instrumentActivate;
    }

    @Override
    public void reset()
    {
        record = null;
    }
}
