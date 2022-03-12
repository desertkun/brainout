package com.desertkun.brainout.content.bullet;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pool;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.bullet.LimitedBulletData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.bullet.LimitedBullet")
public class LimitedBullet extends Bullet
{
    private Pool<BulletData> bulletPool;
    private float maxDistance;

    public LimitedBullet()
    {
        super();

        bulletPool = new Pool<BulletData>(64)
        {
            @Override
            protected LimitedBulletData newObject()
            {
                return new LimitedBulletData(LimitedBullet.this, this);
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

        maxDistance = jsonData.getFloat("maxDistance");
    }

    public float getMaxDistance()
    {
        return maxDistance;
    }
}
