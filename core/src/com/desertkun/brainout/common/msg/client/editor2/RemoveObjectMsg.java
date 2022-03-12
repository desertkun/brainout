package com.desertkun.brainout.common.msg.client.editor2;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.data.active.ActiveData;

public class RemoveObjectMsg implements ModeMessage
{
    public String d;
    public int o;

    public RemoveObjectMsg() {}
    public RemoveObjectMsg(ActiveData object)
    {
        this.d = object.getDimension();
        this.o = object.getId();
    }
}
