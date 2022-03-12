package com.desertkun.brainout.data.bullet;

import com.badlogic.gdx.utils.Pool;
import com.desertkun.brainout.content.bullet.Bullet;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.bullet.ShotBulletData")
public class ShotBulletData extends BulletData
{
    public ShotBulletData(Bullet content, Pool<BulletData> pool)
    {
        super(content, pool);
    }
}
