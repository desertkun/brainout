package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;

public class KilledByEvent extends Event
{
    public ActiveData killer;
    public InstrumentInfo info;

    @Override
    public ID getID()
    {
        return ID.killedBy;
    }

    private Event init(ActiveData killer, InstrumentInfo info)
    {
        this.killer = killer;
        this.info = info;

        return this;
    }

    public static Event obtain(ActiveData killer, InstrumentInfo info)
    {
        KilledByEvent e = obtain(KilledByEvent.class);
        if (e == null) return null;
        return e.init(killer, info);
    }

    @Override
    public void reset()
    {
        this.killer = null;
        this.info = null;
    }
}
