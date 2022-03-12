package com.desertkun.brainout.events;

public class UpdateTeamVisibilityEvent extends Event
{
    public boolean visible;

    @Override
    public ID getID()
    {
        return ID.teamVisibilityUpdated;
    }

    private Event init(boolean visible)
    {
        this.visible = visible;

        return this;
    }

    public static Event obtain(boolean visible)
    {
        UpdateTeamVisibilityEvent e = obtain(UpdateTeamVisibilityEvent.class);
        if (e == null) return null;
        return e.init(visible);
    }

    @Override
    public void reset()
    {
        this.visible = false;
    }
}
