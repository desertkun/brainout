package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.active.PlayerData;

public class OtherPlayerAimMsg implements UdpMessage
{
    public int object;
    public int d;
    public boolean aim;

    public OtherPlayerAimMsg() {}
    public OtherPlayerAimMsg(PlayerData playerData, boolean aim)
    {
        this.object = playerData.getId();
        this.d = playerData.getDimensionId();
        this.aim = aim;
    }
}
