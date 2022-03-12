package com.desertkun.brainout.common.editor;

import com.desertkun.brainout.common.msg.ModeMessage;

public class DeleteDimensionMsg implements ModeMessage
{
    public String d;

    public DeleteDimensionMsg() {}
    public DeleteDimensionMsg(String dimension)
    {
        this.d = dimension;
    }
}
