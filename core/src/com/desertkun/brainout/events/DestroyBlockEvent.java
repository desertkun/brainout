package com.desertkun.brainout.events;

import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.instrument.InstrumentInfo;

public class DestroyBlockEvent extends Event
{
    public Map map;
    public int x;
    public int y;
    public int layer;

    public boolean notify;

    @Override
    public ID getID()
    {
        return ID.destroyBlock;
    }

    private Event init(Map map, int x, int y, int layer, boolean notify)
    {
        this.map = map;
        this.x = x;
        this.y = y;
        this.layer = layer;

        return this;
    }

    public static Event obtain(Map map, int x, int y, int layer, boolean notify)
    {
        DestroyBlockEvent e = obtain(DestroyBlockEvent.class);
        if (e == null) return null;
        return e.init(map, x, y, layer, notify);
    }

    @Override
    public void reset()
    {
        this.map = null;
    }
}
