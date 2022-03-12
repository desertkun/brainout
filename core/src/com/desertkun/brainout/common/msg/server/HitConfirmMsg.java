package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;

public class HitConfirmMsg implements UdpMessage
{
    public String collider;
    public int d;
    public int obj;
    public float x, y;
    public int dmg;

    public HitConfirmMsg() {}
    public HitConfirmMsg(String collider, int d, int obj, float x, float y, int dmg)
    {
        this.collider = collider;
        this.d = d;
        this.obj = obj;
        this.x = x;
        this.y = y;
        this.dmg = dmg;
    }
}
