package com.desertkun.brainout.data.bullet;

import com.badlogic.gdx.utils.Pool;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.bullet.ExplosiveBullet;
import com.desertkun.brainout.events.DestroyEvent;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.bullet.ExplosiveBulletData")
public class ExplosiveBulletData extends ShotBulletData
{
    private float activationTime;

    public ExplosiveBulletData(ExplosiveBullet content, Pool<BulletData> pool)
    {
        super(content, pool);

        this.activationTime = content.getActivationTime();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        activationTime -= dt;
    }

    @Override
    public void release()
    {
        super.release();

        if (activationTime <= 0)
        {
            BrainOut.EventMgr.sendDelayedEvent(this, DestroyEvent.obtain());
        }
    }
}
