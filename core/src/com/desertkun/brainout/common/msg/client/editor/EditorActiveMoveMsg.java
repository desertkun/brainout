package com.desertkun.brainout.common.msg.client.editor;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.data.active.ActiveData;

public class EditorActiveMoveMsg implements ModeMessage
{
    public float x;
    public float y;
    public int id;
    public String d;

    public EditorActiveMoveMsg() {}
    public EditorActiveMoveMsg(ActiveData activeData, Vector2 position)
    {
        this.id = activeData.getId();
        this.x = position.x;
        this.y = position.y;
        this.d = activeData.getDimension();
    }
}
