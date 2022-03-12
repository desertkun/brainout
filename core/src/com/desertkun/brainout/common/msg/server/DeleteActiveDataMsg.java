package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.active.ActiveData;

public class DeleteActiveDataMsg
{
    public int id;
    public int d;
    public boolean ragdoll;

    public DeleteActiveDataMsg() {}
    public DeleteActiveDataMsg(ActiveData activeData, boolean ragdoll)
    {
        this.id = activeData.getId();
        this.d = activeData.getDimensionId();
        this.ragdoll = ragdoll;
    }
}
