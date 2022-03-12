package com.desertkun.brainout.content.bullet;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.bullet.ExplosiveBulletData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.bullet.ExplosiveBullet")
public class ExplosiveBullet extends ShotBullet
{
    private final Pool<BulletData> bulletPool;
    private float activationTime;

    public ExplosiveBullet()
    {
        super();

        bulletPool = new Pool<BulletData>(64)
        {
            @Override
            protected ExplosiveBulletData newObject()
            {
                return new ExplosiveBulletData(ExplosiveBullet.this, this);
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

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        activationTime = jsonData.getFloat("activationTime", 0);
    }

    public float getActivationTime()
    {
        return activationTime;
    }
}
