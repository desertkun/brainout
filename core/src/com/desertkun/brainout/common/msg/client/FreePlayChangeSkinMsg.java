package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class FreePlayChangeSkinMsg implements ModeMessage
{
    public int object;

    public FreePlayChangeSkinMsg() {}
    public FreePlayChangeSkinMsg(ConsumableRecord record)
    {
        this.object = record.getId();
    }
}
