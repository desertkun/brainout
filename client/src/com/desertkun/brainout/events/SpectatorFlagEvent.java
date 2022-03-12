package com.desertkun.brainout.events;

public class SpectatorFlagEvent extends Event
{
    public boolean flag;

    @Override
    public ID getID()
    {
        return ID.spectatorFlag;
    }

    private Event init(boolean flag)
    {
        this.flag = flag;

        return this;
    }

    public static Event obtain(boolean flag)
    {
        SpectatorFlagEvent e = obtain(SpectatorFlagEvent.class);
        if (e == null) return null;
        return e.init(flag);
    }

    @Override
    public void reset()
    {
        this.flag = false;
    }
}
