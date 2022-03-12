package com.desertkun.brainout.common.msg.client.editor;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.data.active.ActiveData;

public class EditorActiveRemoveMsg implements ModeMessage
{
    public int id;
    public String d;

    public EditorActiveRemoveMsg() {}
    public EditorActiveRemoveMsg(ActiveData activeData)
    {
        this.id = activeData.getId();
        this.d = activeData.getDimension();
    }
}
