package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.Map;

public class BlockAddMsg
{
    public int x;
    public int y;
    public int layer;
    public String data;
    public int d;

    public BlockAddMsg() {}
    public BlockAddMsg(int x, int y, int layer, String data, String dimension)
    {
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.data = data;
        this.d = Map.GetDimensionId(dimension);
    }
}
