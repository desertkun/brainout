package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.active.ActiveData;

public class UpdatedActiveDataMsg
{
    public int id;
    public int d;
    public String data;

    public UpdatedActiveDataMsg() {}
    public UpdatedActiveDataMsg(ActiveData activeData, ActiveData.ComponentWriter componentWriter, int owner)
    {
        this.id = activeData.getId();
        this.d = activeData.getDimensionId();
        this.data = Data.ComponentSerializer.toJson(activeData, componentWriter, owner);
    }
}
