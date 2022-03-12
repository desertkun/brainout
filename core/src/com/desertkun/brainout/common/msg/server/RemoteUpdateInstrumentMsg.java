package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.instrument.InstrumentData;

public class RemoteUpdateInstrumentMsg implements UdpMessage
{
    public int object;
    public int d;
    public String current;
    public String hooked;

    public RemoteUpdateInstrumentMsg() {}

    public RemoteUpdateInstrumentMsg(ActiveData activeData, InstrumentData current, InstrumentData hooked)
    {
        this.object = activeData.getId();
        this.d = activeData.getDimensionId();
        this.current = current != null ? BrainOut.R.JSON.toJson(current) : null;
        this.hooked = hooked != null ? BrainOut.R.JSON.toJson(hooked) : null;
    }
}
