package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;

public class SlowmoMsg implements UdpMessage
{
    public float slowmo;

    public SlowmoMsg() {}
    public SlowmoMsg(float slowmo)
    {
        this.slowmo = slowmo;
    }
}
