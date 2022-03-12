package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;

public class OwnerChangedEvent extends Event
{
    public ActiveData newOwner;

    @Override
    public ID getID()
    {
        return ID.ownerChanged;
    }

    private Event init(ActiveData newOwner)
    {
        this.newOwner = newOwner;

        return this;
    }

    public static Event obtain(ActiveData newOwner)
    {
        OwnerChangedEvent e = obtain(OwnerChangedEvent.class);
        if (e == null) return null;
        return e.init(newOwner);
    }

    @Override
    public void reset()
    {
        this.newOwner = null;
    }
}
