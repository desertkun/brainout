package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.FlippedAngle;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class OtherPlayerBulletLaunch implements UdpMessage
{
    public float x;
    public float y;
    public float activeX;
    public float activeY;
    public float angles[];

    public int object;
    public int d;
    public String instrumentId;
    public String bulletId;
    public int ownerId;
    public Bullet.BulletSlot slot;
    public boolean silent;

    public OtherPlayerBulletLaunch() {}
    public OtherPlayerBulletLaunch(PlayerData playerData,
        float x, float y, float activeX, float activeY, float angles[], InstrumentData instrumentData,
        Bullet bullet, Bullet.BulletSlot slot, boolean silent)
    {
        this.object = playerData != null ? playerData.getId() : -1;
        this.d = playerData != null ? playerData.getDimensionId() : -1;
        this.ownerId = playerData.getOwnerId();
        this.silent = silent;

        this.x = x;
        this.y = y;
        this.activeX = activeX;
        this.activeY = activeY;
        this.angles = angles;
        this.slot = slot;

        this.instrumentId = instrumentData != null ? instrumentData.getInstrument().getID() : null;
        this.bulletId = bullet.getID();
    }
}
