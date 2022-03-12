package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class TakeRecordFromItemMsg
{
    public int object;
    public int record;
    public int amount;

    public TakeRecordFromItemMsg() {}
    public TakeRecordFromItemMsg(int object, ConsumableRecord record, int amount)
    {
        this.object = object;
        this.record = record.getId();
        this.amount = amount;
    }
}
