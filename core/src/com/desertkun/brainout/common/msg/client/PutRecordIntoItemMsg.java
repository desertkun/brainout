package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class PutRecordIntoItemMsg
{
    public int object;
    public int record;
    public int amount;

    public PutRecordIntoItemMsg() {}
    public PutRecordIntoItemMsg(int object, ConsumableRecord record, int amount)
    {
        this.object = object;
        this.record = record.getId();
        this.amount = amount;
    }
}
