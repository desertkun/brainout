package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.ModeMessage;

public class TeamLandingMsg implements ModeMessage
{
    public int clientId;
    public long time;

    public TeamLandingMsg() {}
    public TeamLandingMsg(int clientId, long time)
    {
        this.clientId = clientId;
        this.time = time;
    }
}
