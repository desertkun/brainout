package com.desertkun.brainout.events;

import com.desertkun.brainout.content.Team;

public class TeamWonEvent extends Event
{
    public Team team;

    @Override
    public ID getID()
    {
        return ID.teamWon;
    }

    private Event init(Team team)
    {
        this.team = team;

        return this;
    }

    public static Event obtain(Team team)
    {
        TeamWonEvent e = obtain(TeamWonEvent.class);
        if (e == null) return null;
        return e.init(team);
    }

    @Override
    public void reset()
    {
        this.team = null;
    }
}
