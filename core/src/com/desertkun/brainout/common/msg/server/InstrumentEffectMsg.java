package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.instrument.InstrumentData;

public class InstrumentEffectMsg implements UdpMessage
{
    public int object;
    public int d;
    public String instrumentId;
    public String effect;

    public InstrumentEffectMsg() {}
    public InstrumentEffectMsg(ActiveData playerData, InstrumentData instrumentData, String effect)
    {
        this.object = playerData != null ? playerData.getId() : -1;
        this.d = playerData != null ? playerData.getDimensionId() : -1;

        this.instrumentId = instrumentData != null ? instrumentData.getInstrument().getID() : null;
        this.effect = effect;
    }
}
