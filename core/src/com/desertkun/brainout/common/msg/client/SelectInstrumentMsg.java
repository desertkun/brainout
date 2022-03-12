package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class SelectInstrumentMsg
{
    public int id;

    public SelectInstrumentMsg() {}
    public SelectInstrumentMsg(ConsumableRecord record)
    {
        this.id = record != null ? record.getId() : -1;
    }
}
