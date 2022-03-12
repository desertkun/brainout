package com.desertkun.brainout.common.editor;

import com.desertkun.brainout.common.msg.ModeMessage;

public class ResizeMapMsg implements ModeMessage
{
    public String d;
    public int w, h, aX, aY;

    public ResizeMapMsg() {}

    public ResizeMapMsg(String dimension, int width, int height, int anchorX, int anchorY)
    {
        this.d = dimension;
        this.w = width;
        this.h = height;
        this.aX = anchorX;
        this.aY = anchorY;
    }
}
