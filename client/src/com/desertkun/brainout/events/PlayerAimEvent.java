package com.desertkun.brainout.events;

public class PlayerAimEvent extends Event
{
    public boolean aim;

    @Override
    public ID getID()
    {
        return ID.aim;
    }

    private Event init(boolean aim)
    {
        this.aim = aim;

        return this;
    }

    public static Event obtain(boolean aim)
    {
        PlayerAimEvent e = obtain(PlayerAimEvent.class);
        if (e == null) return null;
        return e.init(aim);
    }

    @Override
    public void reset()
    {
        this.aim = false;
    }
}
