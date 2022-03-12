package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.RadioactiveComponentData;
import com.desertkun.brainout.data.components.WindComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.WindComponent")
public class WindComponent extends ContentComponent
{
    private float distance;
    private float power;

    @Override
    public WindComponentData getComponent(ComponentObject componentObject)
    {
        return new WindComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        distance = jsonData.getFloat("distance");
        power = jsonData.getFloat("power");
    }

    public float getDistance()
    {
        return distance;
    }

    public float getPower()
    {
        return power;
    }
}
