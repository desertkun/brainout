package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.TimeToLiveComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.TimeToLiveComponent")
public class TimeToLiveComponent extends ContentComponent
{
    private float time;
    private float random;

    @Override
    public TimeToLiveComponentData getComponent(ComponentObject componentObject)
    {
        return new TimeToLiveComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.time = jsonData.getFloat("time", 0);
        this.random = jsonData.getFloat("random", 0);
    }

    public float getTime()
    {
        if (random != 0)
        {
            return time + MathUtils.random(random);
        }

        return time;
    }
}
