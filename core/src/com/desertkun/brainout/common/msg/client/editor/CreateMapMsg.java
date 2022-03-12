package com.desertkun.brainout.common.msg.client.editor;

import com.desertkun.brainout.common.msg.ModeMessage;

public class CreateMapMsg implements ModeMessage
{
    public String mapName;
    public int mapWidth;
    public int mapHeight;

    public CreateMapMsg() {}
    public CreateMapMsg(String mapName, int mapWidth, int mapHeight)
    {
        this.mapName = mapName;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }
}
