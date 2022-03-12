package com.desertkun.brainout.common.msg.client.editor2;

import com.desertkun.brainout.content.block.Block;

public class BlockRectMsg extends BlockMessage
{
    public int x, y, w, h;
    public String block;

    public BlockRectMsg() {}
    public BlockRectMsg(String dimension, Block block, int x, int y, int w, int h)
    {
        super(dimension);

        this.block = block != null ? block.getID() : null;

        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
}