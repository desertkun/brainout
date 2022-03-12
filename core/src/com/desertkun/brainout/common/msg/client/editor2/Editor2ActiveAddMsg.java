package com.desertkun.brainout.common.msg.client.editor2;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;

public class Editor2ActiveAddMsg implements ModeMessage
{
    public float x;
    public float y;
    public String id;
    public String d;

    public Editor2ActiveAddMsg() {}
    public Editor2ActiveAddMsg(Active active, float x, float y, String dimension)
    {
        this.id = active.getID();
        this.x = x;
        this.y = y;
        this.d = dimension;
    }
}
