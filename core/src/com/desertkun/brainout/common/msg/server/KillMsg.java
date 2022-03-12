package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;

public class KillMsg implements UdpMessage
{
    public int killerId;
    public int victimId;
    public String weapon;
    public String weaponSkin;
    public float slowmo;

    public ActiveData.LastHitKind kind;

    public KillMsg() {}
    public KillMsg(int killerId, int victimId, InstrumentInfo info,
                   ActiveData.LastHitKind kind, float slowmo)
    {
        this.killerId = killerId;
        this.victimId = victimId;
        this.weapon = info.instrument.getID();
        this.weaponSkin = info.skin.getID();
        this.kind = kind;
        this.slowmo = slowmo;
    }
}
