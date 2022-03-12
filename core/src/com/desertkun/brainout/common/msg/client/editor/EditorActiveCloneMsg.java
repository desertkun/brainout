package com.desertkun.brainout.common.msg.client.editor;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.data.active.ActiveData;

public class EditorActiveCloneMsg extends EditorActiveMoveMsg
{
    public EditorActiveCloneMsg() {}
    public EditorActiveCloneMsg(ActiveData activeData, Vector2 position)
    {
        super(activeData, position);
    }
}
