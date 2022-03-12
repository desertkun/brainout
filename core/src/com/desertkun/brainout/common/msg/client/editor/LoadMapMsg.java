package com.desertkun.brainout.common.msg.client.editor;

import com.desertkun.brainout.common.msg.ModeMessage;

public class LoadMapMsg implements ModeMessage
{
    public String map;

    public LoadMapMsg() {}
    public LoadMapMsg(String map)
    {
        this.map = map;
    }
}
