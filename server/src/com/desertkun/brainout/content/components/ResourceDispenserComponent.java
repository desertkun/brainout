package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ResourceDispenserComponent")
public class ResourceDispenserComponent extends ContentComponent
{
    private float period;
    private float distance;
    private float timeToLive;
    private String effect;
    private int rewardOwner;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.period = jsonData.getFloat("period", 1.0f);
        this.distance = jsonData.getFloat("distance", 8.0f);
        this.timeToLive = jsonData.getFloat("timeToLive", 60.f);
        this.effect = jsonData.getString("effect", null);
        this.rewardOwner = jsonData.getInt("rewardOwner", 0);
    }

    public float getPeriod()
    {
        return period;
    }

    public float getDistance()
    {
        return distance;
    }

    public float getTimeToLive()
    {
        return timeToLive;
    }

    public String getEffect()
    {
        return effect;
    }

    public int getRewardOwner()
    {
        return rewardOwner;
    }
}
