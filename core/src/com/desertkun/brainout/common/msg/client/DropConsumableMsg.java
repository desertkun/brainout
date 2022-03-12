package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class DropConsumableMsg
{
    public int id;
    public int amount;
    public float angle;

    public DropConsumableMsg()
    {

    }

    public DropConsumableMsg(ConsumableRecord record, int amount, float angle)
    {
        this.id = record.getId();
        this.amount = amount;
        this.angle = angle;
    }
}
