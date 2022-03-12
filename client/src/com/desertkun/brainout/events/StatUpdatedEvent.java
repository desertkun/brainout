package com.desertkun.brainout.events;

public class StatUpdatedEvent extends Event
{
    public String statId;
    public float value;

    @Override
    public ID getID()
    {
        return ID.statUpdated;
    }

    private Event init(String statId, float value)
    {
        this.statId = statId;
        this.value = value;

        return this;
    }

    public static Event obtain(String statId, float value)
    {
        StatUpdatedEvent e = obtain(StatUpdatedEvent.class);
        if (e == null) return null;
        return e.init(statId, value);
    }

    @Override
    public void reset()
    {
        statId = null;
        value = 0;
    }
}
