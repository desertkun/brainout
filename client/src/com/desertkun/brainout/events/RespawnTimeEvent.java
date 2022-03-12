package com.desertkun.brainout.events;

public class RespawnTimeEvent extends Event
{
    public float time;

    @Override
    public ID getID()
    {
        return ID.respawnIn;
    }

    private Event init(float time)
    {
        this.time = time;

        return this;
    }

    public static Event obtain(float time)
    {
        RespawnTimeEvent e = obtain(RespawnTimeEvent.class);
        if (e == null) return null;
        return e.init(time);
    }

    @Override
    public void reset()
    {
        this.time = 0;
    }
}
