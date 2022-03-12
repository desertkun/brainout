package com.desertkun.brainout.common.msg.client.editor2;

import com.desertkun.brainout.common.msg.ModeMessage;

public class SpawnEditor2Msg implements ModeMessage
{
    public float x, y;
    public int d;

    public SpawnEditor2Msg() {}
    public SpawnEditor2Msg(float x, float y, int dimension)
    {
        this.x = x;
        this.y = y;
        this.d = dimension;
    }
}
