package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.HealthComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.HealthComponent")
public class HealthComponent extends ContentComponent
{
    private float health;
    private ObjectMap<String, Float> damageCoef;
    private float immortalTime;

    public HealthComponent()
    {
        damageCoef = new ObjectMap<String, Float>();
    }

    @Override
    public HealthComponentData getComponent(ComponentObject componentObject)
    {
        return new HealthComponentData<>(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        health = jsonData.getFloat("health");
        immortalTime = jsonData.getFloat("immortalTime", 0);

        if (jsonData.has("damageCoef"))
        {
            JsonValue dcData = jsonData.get("damageCoef");

            if (dcData.isObject())
            {
                for (JsonValue jv: dcData)
                {
                    damageCoef.put(jv.name(), jv.asFloat());
                }
            }
        }
    }

    public float getHealth()
    {
        return health;
    }

    public ObjectMap<String, Float> getDamageCoef()
    {
        return damageCoef;
    }

    public float getImmortalTime()
    {
        return immortalTime;
    }
}
