package com.desertkun.brainout.common.msg.client.editor2;

import com.desertkun.brainout.common.msg.ModeMessage;

public class BlockMessage implements ModeMessage
{
    public String d;

    public BlockMessage() {}
    public BlockMessage(String dimension)
    {
        this.d = dimension;
    }
}
