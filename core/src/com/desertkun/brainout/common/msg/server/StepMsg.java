package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;

public class StepMsg implements UdpMessage
{
    public int i;
    public float v;
    public float p;
    public int d;

    public StepMsg() {}
    public StepMsg(int i, float v, float p, int d)
    {
        this.i = i;
        this.v = v;
        this.p = p;
        this.d = d;
    }
}
