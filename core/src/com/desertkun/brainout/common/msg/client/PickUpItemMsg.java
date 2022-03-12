package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;

public class PickUpItemMsg implements UdpMessage
{
    public int object;

    public PickUpItemMsg() {}
    public PickUpItemMsg(int object)
    {
        this.object = object;
    }
}
