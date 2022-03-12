package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;

public class PlayerAimMsg implements UdpMessage
{
    public boolean aim;

    public PlayerAimMsg() {}
    public PlayerAimMsg(boolean aim)
    {
        this.aim = aim;
    }
}
