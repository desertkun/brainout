package com.desertkun.brainout.common.enums.data;

import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class ConsumableND extends NotifyData
{
    public ConsumableRecord item;

    public ConsumableND(ConsumableRecord item)
    {
        this.item = item;
    }
}
