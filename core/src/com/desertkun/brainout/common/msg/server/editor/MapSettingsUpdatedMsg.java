package com.desertkun.brainout.common.msg.server.editor;

import com.desertkun.brainout.common.msg.ModeMessage;

public class MapSettingsUpdatedMsg implements ModeMessage
{
    public String data;
    public String d;

    public MapSettingsUpdatedMsg() {}
    public MapSettingsUpdatedMsg(String data, String dimension)
    {
        this.data = data;
        this.d = dimension;
    }
}
