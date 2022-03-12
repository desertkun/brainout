package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class SimpleInstrumentActionMsg implements UdpMessage
{
    public int recordId;
    public Instrument.Action action;

    public SimpleInstrumentActionMsg() {}
    public SimpleInstrumentActionMsg(ConsumableRecord record, Instrument.Action action)
    {
        this.recordId = record.getId();
        this.action = action;
    }
}
