package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.content.shop.Slot;

public class SelectCategoryMsg
{
    public String slot;
    public String category;

    public SelectCategoryMsg(Slot slot, Slot.Category category)
    {
        this.slot = slot.getID();
        this.category = category != null ? category.getId() : null;
    }

    public SelectCategoryMsg() {}
}
