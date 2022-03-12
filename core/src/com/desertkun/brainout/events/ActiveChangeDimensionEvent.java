package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.ActiveData;

public class ActiveChangeDimensionEvent extends Event
{
    public ActiveData activeData;
    public int oldId;
    public String oldDimension;
    public String newDimension;

    @Override
    public void reset()
    {
        activeData = null;
        oldId = -1;
        oldDimension = null;
        newDimension = null;
    }

    private Event init(ActiveData activeData, int oldId, String oldDimension, String newDimension)
    {
        this.activeData = activeData;
        this.oldId = oldId;
        this.oldDimension = oldDimension;
        this.newDimension = newDimension;

        return this;
    }

    public static Event obtain(ActiveData activeData, int oldId, String oldDimension, String newDimension)
    {
        ActiveChangeDimensionEvent e = obtain(ActiveChangeDimensionEvent.class);
        if (e == null) return null;
        return e.init(activeData, oldId, oldDimension, newDimension);
    }

    @Override
    public ID getID()
    {
        return ID.activeChangeDimension;
    }
}
