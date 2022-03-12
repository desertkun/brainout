package com.desertkun.brainout.common.msg.client.editor2;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.content.Content;

public class CreateObjectMsg implements ModeMessage
{
    public String d;
    public String o;
    public int x;
    public int y;

    public CreateObjectMsg() {}
    public CreateObjectMsg(String dimension, Content object, int x, int y)
    {
        this.d = dimension;
        this.o = object.getID();
        this.x = x;
        this.y = y;
    }
}
