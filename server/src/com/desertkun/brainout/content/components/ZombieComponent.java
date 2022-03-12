package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ZombieComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RandomValue;

@Reflect("content.components.ZombieComponent")
public class ZombieComponent extends ContentComponent
{
    private String moanEffect;
    private RandomValue moanPeriod, attackTime, fireTime, firePauseTime;
    private String noticeEffect;

    public ZombieComponent()
    {
        moanPeriod = new RandomValue();
        attackTime = new RandomValue();
        fireTime = new RandomValue();
        firePauseTime = new RandomValue();
    }

    @Override
    public ZombieComponentData getComponent(ComponentObject componentObject)
    {
        return new ZombieComponentData((PlayerData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        moanEffect = jsonData.getString("moan");
        noticeEffect = jsonData.getString("notice");
        moanPeriod.read(json, jsonData.get("moanPeriod"));
        attackTime.read(json, jsonData.get("attackTime"));
        fireTime.read(json, jsonData.get("fireTime"));
        firePauseTime.read(json, jsonData.get("firePauseTime"));
    }

    public RandomValue getMoanPeriod()
    {
        return moanPeriod;
    }

    public RandomValue getAttackTime()
    {
        return attackTime;
    }

    public String getMoanEffect()
    {
        return moanEffect;
    }

    public String getNoticeEffect()
    {
        return noticeEffect;
    }

    public RandomValue getFireTime()
    {
        return fireTime;
    }

    public RandomValue getFirePauseTime()
    {
        return firePauseTime;
    }
}
