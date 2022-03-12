package com.desertkun.brainout.events;

public class SelectSlotEvent extends Event
{
    public int slot;
    public String mode;

    @Override
    public ID getID()
    {
        return ID.selectSlot;
    }

    private Event init(int slot, String mode)
    {
        this.slot = slot;
        this.mode = mode;

        return this;
    }

    public static Event obtain(int slot, String mode)
    {
        SelectSlotEvent e = obtain(SelectSlotEvent.class);
        if (e == null) return null;
        return e.init(slot, mode);
    }

    public static Event obtain(int slot)
    {
        SelectSlotEvent e = obtain(SelectSlotEvent.class);
        if (e == null) return null;
        return e.init(slot, null);
    }

    @Override
    public void reset()
    {
        this.slot = 0;
        this.mode = null;
    }
}
