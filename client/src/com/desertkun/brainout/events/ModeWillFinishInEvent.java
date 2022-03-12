package com.desertkun.brainout.events;

public class ModeWillFinishInEvent extends Event
{
    public float time;

    @Override
    public ID getID()
    {
        return ID.modeWillFinish;
    }

    private Event init(float time)
    {
        this.time = time;

        return this;
    }

    public static Event obtain(float time)
    {
        ModeWillFinishInEvent e = obtain(ModeWillFinishInEvent.class);
        if (e == null) return null;
        return e.init(time);
    }

    @Override
    public void reset()
    {
        this.time = 0;
    }
}
