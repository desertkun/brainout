package com.desertkun.brainout.common.msg.client.editor;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Active;

public class EditorActiveAddMsg implements ModeMessage
{
    public float x;
    public float y;
    public String id;
    public String team;
    public int layer;
    public String d;

    public EditorActiveAddMsg() {}
    public EditorActiveAddMsg(Active active, int layer, Team team, float x, float y, String dimension)
    {
        this.id = active.getID();
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.team = team == null ? null : team.getID();
        this.d = dimension;
    }
}
