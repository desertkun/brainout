package com.desertkun.brainout.common.msg.client.editor;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.content.block.Block;

public class EditorBlockMsg implements ModeMessage
{
    public int x;
    public int y;
    public int layer;
    public String block;
    public String d;

    public EditorBlockMsg() {}
    public EditorBlockMsg(int x, int y, int layer, Block block, String dimension)
    {
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.block = block != null ? block.getID() : null;
        this.d = dimension;
    }
}
