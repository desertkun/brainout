package com.desertkun.brainout.events;

import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Map;

public class SetDirtyEvent extends Event
{
    public int x;
    public int y;
    public int layer;

    @Override
    public ID getID()
    {
        return ID.setDirty;
    }

    private Event init(int x, int y, int layer)
    {
        this.x = x;
        this.y = y;
        this.layer = layer;

        return this;
    }

    public static Event obtain(int x, int y, int layer, Map map)
    {
        SetDirtyEvent e = obtain(SetDirtyEvent.class);
        if (e == null) return null;
        return e.init(x, y, layer);
    }

    @Override
    public void reset()
    {
        this.x = 0;
        this.y = 0;
        this.layer = 0;
    }
}
