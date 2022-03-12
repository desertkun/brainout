package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.Map;

public class BlockDestroyMsg
{
    public int x;
    public int y;
    public int layer;
    public int d;
    public String test;

    public BlockDestroyMsg() {}
    public BlockDestroyMsg(int x, int y, int layer, String dimension)
    {
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.d = Map.GetDimensionId(dimension);

        this.test = "" + x + y + layer + dimension;
    }

    public boolean check()
    {
        return this.test.equals("" + x + y + layer + d);
    }
}
