package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.active.PlayerData;

public class PlayerWoundedMsg
{
    public int object;
    public int d;
    public boolean wounded;

    public PlayerWoundedMsg() {}
    public PlayerWoundedMsg(PlayerData playerData)
    {
        this.object = playerData.getId();
        this.d = playerData.getDimensionId();
        this.wounded = playerData.isWounded();
    }
}
