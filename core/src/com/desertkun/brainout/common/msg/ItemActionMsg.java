package com.desertkun.brainout.common.msg;

import com.desertkun.brainout.data.active.ActiveData;

public class ItemActionMsg
{
    public int object;
    public int d;
    public String action;

    public ItemActionMsg() {}
    public ItemActionMsg(ActiveData itemData, String action)
    {
        this.action = action;
        this.object = itemData.getId();
        this.d = itemData.getDimensionId();
    }
}
