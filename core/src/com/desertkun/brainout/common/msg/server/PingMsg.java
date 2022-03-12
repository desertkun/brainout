package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;

public class PingMsg implements UdpMessage
{
    public long timeStamp;

    public PingMsg() {}
    public PingMsg(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }
}
