package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.TraceEffectComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.TraceEffectComponent")
public class TraceEffectComponent extends ContentComponent
{
    private String effectVisible, effectInvisible;
    private float distance;

    @Override
    public TraceEffectComponentData getComponent(ComponentObject componentObject)
    {
        return new TraceEffectComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.effectVisible = jsonData.getString("effectVisible");
        this.effectInvisible = jsonData.getString("effectInvisible");
        this.distance = jsonData.getFloat("distance");
    }

    public String getEffect(boolean visible)
    {
        return visible ? effectVisible : effectInvisible;
    }

    public float getDistance()
    {
        return distance;
    }
}
