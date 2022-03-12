package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.common.msg.UdpMessage;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class BulletLaunchMsg implements UdpMessage
{
    public float x;
    public float y;
    public float activeX;
    public float activeY;
    public float angles[];

    public int recordId;
    public int bullets;
    public int random;
    public String bullet;
    public Bullet.BulletSlot slot;

    public enum Slot
    {
        primary,
        secondary
    }

    public BulletLaunchMsg() {}
    public BulletLaunchMsg(LaunchData launchData, ActiveData active,
                           float[] angles, ConsumableRecord record, Bullet bullet, Bullet.BulletSlot slot, int bullets,
                           int random)
    {
        this.x = launchData.getX();
        this.y = launchData.getY();
        this.activeX = active.getX();
        this.activeY = active.getY();
        this.angles = angles;
        this.random = random;

        this.recordId = record.getId();
        this.bullet = bullet.getID();
        this.slot = slot;
        this.bullets = bullets;
    }
}
