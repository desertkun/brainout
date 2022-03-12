package com.desertkun.brainout.events;

import com.desertkun.brainout.content.instrument.Instrument;

public class InstrumentActionEvent extends Event
{
    public Instrument.Action action;
    public float data0, data1;

    @Override
    public ID getID()
    {
        return ID.instrumentAction;
    }

    private Event init(Instrument.Action action)
    {
        this.action = action;
        this.data0 = 0;
        this.data1 = 0;

        return this;
    }

    private Event init(Instrument.Action action, float data0, float data1)
    {
        this.action = action;
        this.data0 = data0;
        this.data1 = data1;

        return this;
    }

    public static Event obtain(Instrument.Action action)
    {
        InstrumentActionEvent e = obtain(InstrumentActionEvent.class);
        if (e == null) return null;
        return e.init(action);
    }

    public static Event obtain(Instrument.Action action, float data0, float data1)
    {
        InstrumentActionEvent e = obtain(InstrumentActionEvent.class);
        if (e == null) return null;
        return e.init(action, data0, data1);
    }

    @Override
    public void reset()
    {
        action = null;
        data0 = 0;
        data1 = 0;
    }
}
