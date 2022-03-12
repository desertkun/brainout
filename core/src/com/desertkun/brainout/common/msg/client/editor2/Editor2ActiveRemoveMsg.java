package com.desertkun.brainout.common.msg.client.editor2;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.data.active.ActiveData;

public class Editor2ActiveRemoveMsg implements ModeMessage
{
    public int id;
    public String d;

    public Editor2ActiveRemoveMsg() {}
    public Editor2ActiveRemoveMsg(ActiveData activeData)
    {
        this.id = activeData.getId();
        this.d = activeData.getDimension();
    }
}
