package com.desertkun.brainout.events;

import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.active.FlagData;

public class FlagTakenEvent extends Event
{
    public Team team;
    public FlagData flagData;

    @Override
    public ID getID()
    {
        return ID.flagTaken;
    }

    private Event init(Team team, FlagData flagData)
    {
        this.team = team;
        this.flagData = flagData;

        return this;
    }

    public static Event obtain(Team team, FlagData flagData)
    {
        FlagTakenEvent e = obtain(FlagTakenEvent.class);
        if (e == null) return null;
        return e.init(team, flagData);
    }

    @Override
    public void reset()
    {
        this.team = null;
        this.flagData = null;
    }
}
