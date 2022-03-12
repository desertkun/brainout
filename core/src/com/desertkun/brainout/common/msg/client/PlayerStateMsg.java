package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.active.Player;

public class PlayerStateMsg implements UdpMessage
{
    public Player.State state;

    public PlayerStateMsg() {}
    public PlayerStateMsg(Player.State state)
    {
        this.state = state;
    }
}
