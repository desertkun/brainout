package com.desertkun.brainout.events;

import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class DamageEvent extends Event
{
    public float damage;
    public int damager;
    public InstrumentInfo info;

    public float x;
    public float y;
    public float angle;

    public BulletData bulletData;
    public String damageKind;

    @Override
    public ID getID()
    {
        return ID.damage;
    }

    private Event init(float damage, int damager, InstrumentInfo info, BulletData bulletData,
                       float x, float y, float angle, String damageKind)
    {
        this.damage = damage;
        this.damager = damager;
        this.info = info;
        this.bulletData = bulletData;

        this.x = x;
        this.y = y;
        this.angle = angle;

        this.damageKind = damageKind;

        return this;
    }

    public static Event obtain(float damage, int damager, InstrumentInfo info, BulletData bulletData,
                               float x, float y, float angle, String damageKind)
    {
        DamageEvent e = obtain(DamageEvent.class);
        if (e == null) return null;
        return e.init(damage, damager, info, bulletData,
                x, y, angle, damageKind);
    }

    @Override
    public void reset()
    {
        this.damage = 0;
        this.damager = 0;
        this.info = null;
        this.bulletData = null;

        this.x = 0;
        this.y = 0;
        this.angle = 0;

        this.damageKind = null;
    }
}
