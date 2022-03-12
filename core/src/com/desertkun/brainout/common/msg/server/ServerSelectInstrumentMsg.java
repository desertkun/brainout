package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class ServerSelectInstrumentMsg
{
    public int instrumentId;

    public ServerSelectInstrumentMsg() {}
    public ServerSelectInstrumentMsg(ConsumableRecord record)
    {
        this.instrumentId = record.getId();
    }
}
