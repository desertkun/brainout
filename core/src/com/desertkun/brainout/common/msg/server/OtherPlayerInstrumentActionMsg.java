package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.instrument.InstrumentData;

public class OtherPlayerInstrumentActionMsg extends ServerPlayerMoveMsg
{
    public String weaponId;
    public Instrument.Action action;
    public float data0;
    public float data1;

    public OtherPlayerInstrumentActionMsg() {}
    public OtherPlayerInstrumentActionMsg(int object, float x, float y,
                                          float speedX, float speedY, float angle,
                                          String dimension,
                                          float aimX, float aimY,
                                          InstrumentData weaponData, Instrument.Action action)
    {
        super(object, x, y, speedX, speedY, angle, dimension, aimX, aimY, false);

        this.weaponId = weaponData != null ? weaponData.getContent().getID() : "";
        this.action = action;
    }

    public void setDataFloat(float data0, float data1)
    {
        this.data0 = data0;
        this.data1 = data1;
    }
}
