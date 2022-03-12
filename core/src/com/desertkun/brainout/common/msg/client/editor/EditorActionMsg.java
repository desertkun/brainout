package com.desertkun.brainout.common.msg.client.editor;

import com.desertkun.brainout.common.msg.ModeMessage;

public class EditorActionMsg implements ModeMessage
{
    public ID id;

    public enum ID
    {
        saveMap,
        unload
    }

    public EditorActionMsg() {}
    public EditorActionMsg(ID id)
    {
        this.id = id;
    }
}
