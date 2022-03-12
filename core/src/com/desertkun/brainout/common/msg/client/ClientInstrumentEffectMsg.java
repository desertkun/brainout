package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.instrument.InstrumentData;

public class ClientInstrumentEffectMsg implements UdpMessage
{
    public int object;
    public String instrument;
    public String effect;

    public ClientInstrumentEffectMsg() {}
    public ClientInstrumentEffectMsg(ActiveData playerData, InstrumentData instrumentData, String effect)
    {
        this.object = playerData != null ? playerData.getId() : -1;

        this.instrument = instrumentData != null ? instrumentData.getInstrument().getID() : null;
        this.effect = effect;
    }
}
