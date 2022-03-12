package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.content.shop.Slot;

public class SelectFavoritesMsg
{
    public String slot;

    public SelectFavoritesMsg(Slot slot)
    {
        this.slot = slot.getID();
    }

    public SelectFavoritesMsg() {}
}
