package com.desertkun.brainout.common.msg.client;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.common.msg.UdpMessage;

public class WatchPointMsg implements UdpMessage
{
    public float x;
    public float y;

    public WatchPointMsg() {}

    public WatchPointMsg(Vector2 point)
    {
        this.x = point.x;
        this.y = point.y;
    }

    public WatchPointMsg(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
}
