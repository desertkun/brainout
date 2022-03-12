package com.desertkun.brainout.events;

public class SelectPreviousSlotEvent extends Event
{
    @Override
    public ID getID()
    {
        return ID.selectPreviousSlot;
    }

    private Event init()
    {
        return this;
    }

    public static Event obtain()
    {
        SelectPreviousSlotEvent e = obtain(SelectPreviousSlotEvent.class);
        if (e == null) return null;
        return e.init();
    }

    @Override
    public void reset()
    {
    }
}
