package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.Map;

public class ServerActiveMoveMsg implements UdpMessage
{
    public int object;

    public float x;
    public float y;
    public float angle;
    public float speedX;
    public float speedY;
    public int d;

    public ServerActiveMoveMsg(){}
    public ServerActiveMoveMsg(int object, float x, float y,
                               float speedX, float speedY, float angle,
                               String dimension)
    {
        this.object = object;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.speedX = speedX;
        this.speedY = speedY;
        this.d = Map.GetDimensionId(dimension);
    }
}