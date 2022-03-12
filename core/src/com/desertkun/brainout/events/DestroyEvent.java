package com.desertkun.brainout.events;

import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class DestroyEvent extends Event
{
    public int destroyer;
    public InstrumentInfo info;

    public float x;
    public float y;
    public float angle;

    public boolean notify;
    public boolean ragdoll;

    @Override
    public ID getID()
    {
        return ID.destroy;
    }

    private Event init()
    {
        this.info = null;
        this.destroyer = -1;

        this.x = 0;
        this.y = 0;
        this.angle = -1;

        this.notify = true;
        this.ragdoll = true;

        return this;
    }

    private Event init(boolean ragdoll)
    {
        this.info = null;
        this.destroyer = -1;

        this.x = 0;
        this.y = 0;
        this.angle = -1;

        this.notify = true;
        this.ragdoll = ragdoll;

        return this;
    }

    private Event init(InstrumentInfo info, int destroyer, float x, float y, float angle, boolean notify)
    {
        this.info = info;
        this.destroyer = destroyer;

        this.x = x;
        this.y = y;
        this.angle = angle;

        this.notify = notify;
        this.ragdoll = true;

        return this;
    }

    public static Event obtain(boolean ragdoll)
    {
        DestroyEvent e = obtain(DestroyEvent.class);
        if (e == null) return null;
        return e.init(ragdoll);
    }

    public static Event obtain(InstrumentInfo info, int destroyer, float x, float y, float angle, boolean notify)
    {
        DestroyEvent e = obtain(DestroyEvent.class);
        if (e == null) return null;
        return e.init(info, destroyer, x, y, angle, notify);
    }

    public static Event obtain()
    {
        DestroyEvent e = obtain(DestroyEvent.class);
        if (e == null) return null;
        return e.init();
    }

    @Override
    public void reset()
    {
        this.info = null;
        this.destroyer = -1;

        this.x = 0;
        this.y = 0;
        this.angle = -1;

        this.notify = true;
    }
}
