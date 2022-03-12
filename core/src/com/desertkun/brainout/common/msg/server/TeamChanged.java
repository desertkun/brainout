package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.content.Team;

public class TeamChanged
{
    public String teamId;

    public TeamChanged() {}
    public TeamChanged(Team team)
    {
        this.teamId = team.getID();
    }
}
