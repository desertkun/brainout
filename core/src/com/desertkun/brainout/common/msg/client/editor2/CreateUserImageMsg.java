package com.desertkun.brainout.common.msg.client.editor2;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.content.Content;

public class CreateUserImageMsg implements ModeMessage
{
    public String d;
    public String s;
    public String c;
    public int x;
    public int y;
    public int w;
    public int h;

    public CreateUserImageMsg() {}
    public CreateUserImageMsg(String dimension, Content content, String sprite, int x, int y, int width, int height)
    {
        this.d = dimension;
        this.s = sprite;
        this.c = content.getID();
        this.x = x;
        this.y = y;
        this.w = width;
        this.h = height;
    }
}
