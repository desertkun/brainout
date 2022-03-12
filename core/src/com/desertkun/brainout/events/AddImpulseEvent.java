package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.ActiveData;

public class AddImpulseEvent extends Event
{
    public float forceX;
    public float forceY;

    @Override
    public ID getID()
    {
        return ID.addImpulse;
    }

    private Event init(float forceX, float forceY)
    {
        this.forceX = forceX;
        this.forceY = forceY;

        return this;
    }

    public static Event obtain(float forceX, float forceY)
    {
        AddImpulseEvent e = obtain(AddImpulseEvent.class);
        if (e == null) return null;
        return e.init(forceX, forceY);
    }

    @Override
    public void reset()
    {
        forceX = 0;
        forceY = 0;
    }
}
