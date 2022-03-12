package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.common.msg.server.PingMsg;

public class PongMsg implements UdpMessage
{
    public long timeStamp;
    public long c;

    public PongMsg() {}
    public PongMsg(PingMsg pingMsg, long c)
    {
        this.timeStamp = pingMsg.timeStamp;
        this.c = c;
    }
}
