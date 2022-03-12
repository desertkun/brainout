package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.content.shop.Slot;

public class SelectTagMsg
{
    public String slot;
    public String tag;

    public SelectTagMsg(Slot slot, Slot.Tag tag)
    {
        this.slot = slot.getID();
        this.tag = tag != null ? tag.getId() : null;
    }

    public SelectTagMsg() {}
}
