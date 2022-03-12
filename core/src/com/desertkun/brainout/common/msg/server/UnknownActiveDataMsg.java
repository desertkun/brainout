package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.Map;

public class UnknownActiveDataMsg implements UdpMessage
{
    public int object;
    public int d;

    public UnknownActiveDataMsg() {}
    public UnknownActiveDataMsg(int object, String dimension)
    {
        this.object = object;
        this.d = Map.GetDimensionId(dimension);
    }
}
