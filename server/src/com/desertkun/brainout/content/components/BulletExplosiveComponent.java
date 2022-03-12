package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.BulletExplosiveComponentData;
import com.desertkun.brainout.data.components.ExplosiveComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BulletExplosiveComponent")
public class BulletExplosiveComponent extends ContentComponent
{
    private Bullet bullet;
    private int amount;
    private String effect;
    private float damage;

    @Override
    public BulletExplosiveComponentData getComponent(ComponentObject componentObject)
    {
        return new BulletExplosiveComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.bullet = ((Bullet) BrainOut.ContentMgr.get(jsonData.getString("bullet")));
        this.damage = jsonData.getInt("damage", 1);
        this.amount = jsonData.getInt("amount");
        this.effect = jsonData.getString("effect", "");
    }

    public Bullet getBullet()
    {
        return bullet;
    }

    public int getAmount()
    {
        return amount;
    }

    public String getEffect()
    {
        return effect;
    }

    public float getDamage()
    {
        return damage;
    }
}
