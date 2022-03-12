package com.desertkun.brainout.common.msg.server;

public class ServerActiveChangeDimensionMsg
{
    public int object;
    public int newObject;

    public String o;
    public String n;

    public float x;
    public float y;
    public float angle;

    public ServerActiveChangeDimensionMsg(){}
    public ServerActiveChangeDimensionMsg(int object, int newObject,
                                          float x, float y, float angle,
                                          String oldDimension, String newDimension)
    {
        this.object = object;
        this.newObject = newObject;
        this.x = x;
        this.y = y;
        this.angle = angle;
        this.o = oldDimension;
        this.n = newDimension;
    }
}