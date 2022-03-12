package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.Map;

public class LaunchEffectMsg implements UdpMessage
{
    public int d;
    public float x;
    public float y;
    public String effect;

    public LaunchEffectMsg() {}
    public LaunchEffectMsg(String dimension, float x, float y, String effect)
    {
        this.d = Map.GetDimensionId(dimension);
        this.x = x;
        this.y = y;
        this.effect = effect;
    }
}
