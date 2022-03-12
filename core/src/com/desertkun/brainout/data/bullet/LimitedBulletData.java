package com.desertkun.brainout.data.bullet;

import com.badlogic.gdx.utils.Pool;
import com.desertkun.brainout.content.bullet.LimitedBullet;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.bullet.LimitedBulletData")
public class LimitedBulletData extends BulletData
{
    private final LimitedBullet bullet;

    public LimitedBulletData(LimitedBullet bullet, Pool<BulletData> pool)
    {
        super(bullet, pool);

        this.bullet = bullet;
    }

    @Override
    public boolean done()
    {
        return super.done() || distance >= bullet.getMaxDistance();
    }
}
