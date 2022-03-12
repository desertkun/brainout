package com.desertkun.brainout.common.msg.server.editor;

import com.desertkun.brainout.common.msg.ModeMessage;

public class MapListMsg implements ModeMessage
{
    public String[] maps;

    public MapListMsg() {}
    public MapListMsg(String[] maps)
    {
        this.maps = maps;
    }
}
