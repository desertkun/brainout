package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.FlippedAngle;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class OtherPlayerInstrumentLaunch implements UdpMessage
{
    public int object;
    public int d;
    public String instrumentId;

    public OtherPlayerInstrumentLaunch() {}
    public OtherPlayerInstrumentLaunch(PlayerData playerData, InstrumentData instrumentData)
    {
        this.object = playerData.getId();
        this.d = playerData.getDimensionId();

        this.instrumentId = instrumentData.getInstrument().getID();
    }
}
