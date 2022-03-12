package com.desertkun.brainout.events;

import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.ColliderComponentData;

public class CollideEvent extends Event
{
    public String colliderName;
    public ColliderComponentData.Collider collider;
    public BulletData bulletData;
    public float x;
    public float y;

    @Override
    public ID getID()
    {
        return ID.collide;
    }

    private Event init(BulletData bulletData, float x, float y,
                       ColliderComponentData.Collider collider,
                       String colliderName)
    {
        this.bulletData = bulletData;
        this.x = x;
        this.y = y;
        this.collider = collider;
        this.colliderName = colliderName;

        return this;
    }

    public static Event obtain(BulletData bulletData, float x, float y,
                               ColliderComponentData.Collider collider,
                               String colliderName)
    {
        CollideEvent e = obtain(CollideEvent.class);
        if (e == null) return null;
        return e.init(bulletData,
                x, y, collider, colliderName);
    }

    @Override
    public void reset()
    {
        this.bulletData = null;
        this.x = 0;
        this.y = 0;
        this.collider = null;
        this.colliderName = null;
    }
}
