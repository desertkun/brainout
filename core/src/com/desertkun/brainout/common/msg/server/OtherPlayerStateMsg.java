package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.data.active.PlayerData;

public class OtherPlayerStateMsg implements UdpMessage
{
    public int object;
    public int d;
    public Player.State state;

    public OtherPlayerStateMsg() {}
    public OtherPlayerStateMsg(PlayerData playerData, Player.State state)
    {
        this.object = playerData.getId();
        this.d = playerData.getDimensionId();
        this.state = state;
    }
}
