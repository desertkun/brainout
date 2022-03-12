package com.desertkun.brainout.common.editor;

import com.desertkun.brainout.common.msg.ModeMessage;

public class NewDimensionMsg implements ModeMessage
{
    public String d;
    public int w, h;

    public NewDimensionMsg() {}
    public NewDimensionMsg(String dimension, int width, int height)
    {
        this.d = dimension;
        this.w = width;
        this.h = height;
    }
}
