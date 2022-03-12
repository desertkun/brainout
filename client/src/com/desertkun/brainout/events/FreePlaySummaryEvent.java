package com.desertkun.brainout.events;

import org.json.JSONObject;

public class FreePlaySummaryEvent extends Event
{
    public JSONObject summary;
    public boolean alive;

    @Override
    public ID getID()
    {
        return ID.freePlaySummary;
    }

    private Event init(JSONObject summary, boolean alive)
    {
        this.summary = summary;
        this.alive = alive;

        return this;
    }

    public static Event obtain(JSONObject summary, boolean alive)
    {
        FreePlaySummaryEvent e = obtain(FreePlaySummaryEvent.class);
        if (e == null) return null;
        return e.init(summary, alive);
    }

    @Override
    public void reset()
    {
        this.summary = null;
        this.alive = false;
    }
}
