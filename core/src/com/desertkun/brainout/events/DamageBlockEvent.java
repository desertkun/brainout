package com.desertkun.brainout.events;

import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;

public class DamageBlockEvent extends Event
{
    public Map map;
    public float damage;
    public int x;
    public int y;
    public int layer;
    public InstrumentInfo info;
    public BulletData bulletData;

    @Override
    public ID getID()
    {
        return ID.damageBlock;
    }

    private Event init(Map map, float damage, int x, int y, int layer, InstrumentInfo info, BulletData bulletData)
    {
        this.map = map;
        this.info = info;
        this.bulletData = bulletData;
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.layer = layer;
        return this;
    }

    public static Event obtain(Map map, float damage, int x, int y, int layer, InstrumentInfo info, BulletData bulletData)
    {
        DamageBlockEvent e = obtain(DamageBlockEvent.class);
        if (e == null) return null;
        return e.init(map, damage, x, y, layer, info, bulletData);
    }

    @Override
    public void reset()
    {
        this.damage = 0;
        this.map = null;
        this.info = null;
        this.bulletData = null;
    }
}
