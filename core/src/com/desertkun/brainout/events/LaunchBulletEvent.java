package com.desertkun.brainout.events;

import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class LaunchBulletEvent extends Event
{
    public float x;
    public float y;
    public float[] angles;
    public int ownerId;
    public Bullet bullet;
    public Bullet.BulletSlot slot;
    public boolean silent;

    @Override
    public ID getID()
    {
        return ID.launchBullet;
    }

    private Event init(Bullet bullet, int ownerId,
                       Bullet.BulletSlot slot, float x, float y, float angles[], boolean silent)
    {
        this.bullet = bullet;
        this.slot = slot;
        this.x = x;
        this.y = y;
        this.angles = angles;
        this.ownerId = ownerId;
        this.silent = silent;

        return this;
    }

    public static Event obtain(Bullet bullet, int ownerId,
                               Bullet.BulletSlot slot, float x, float y, float angles[], boolean silent)
    {
        LaunchBulletEvent e = obtain(LaunchBulletEvent.class);
        if (e == null) return null;
        return e.init(bullet, ownerId, slot, x, y, angles, silent);
    }

    @Override
    public void reset()
    {
        this.bullet = null;
        this.slot = null;
        this.x = 0;
        this.y = 0;
        this.angles = null;
        this.ownerId = -1;
        this.silent = false;
    }
}
