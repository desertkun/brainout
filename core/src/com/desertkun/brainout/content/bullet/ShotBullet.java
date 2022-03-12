package com.desertkun.brainout.content.bullet;

import com.badlogic.gdx.utils.Pool;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.bullet.ShotBulletData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.bullet.ShotBullet")
public class ShotBullet extends Bullet
{
    private Pool<BulletData> bulletPool;

    public ShotBullet()
    {
        super();

        bulletPool = new Pool<BulletData>(64)
        {
            @Override
            protected ShotBulletData newObject()
            {
                return new ShotBulletData(ShotBullet.this, this);
            }
        };
    }

    @Override
    public BulletData getData(LaunchData launchData, float damageCoefficient, String dimension)
    {
        BulletData bulletData = bulletPool.obtain();
        bulletData.setup(launchData, damageCoefficient, dimension);
        
        return bulletData;
    }
}
