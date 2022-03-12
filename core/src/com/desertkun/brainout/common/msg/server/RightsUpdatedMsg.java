package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.online.PlayerRights;

public class RightsUpdatedMsg
{
    public PlayerRights rights;

    public RightsUpdatedMsg() {}
    public RightsUpdatedMsg(PlayerRights rights)
    {
        this.rights = rights;
    }
}
