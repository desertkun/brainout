package com.desertkun.brainout.common.msg.client.editor2;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.content.block.Block;

public class SingleBlockMsg extends BlockMessage
{
    public int x, y;
    public String block;

    public SingleBlockMsg() {}

    public SingleBlockMsg(String dimension, Block block, int x, int y)
    {
        super(dimension);

        this.block = block != null ? block.getID() : null;

        this.x = x;
        this.y = y;
    }
}