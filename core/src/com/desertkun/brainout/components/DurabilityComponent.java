package com.desertkun.brainout.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.components.DurabilityComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.online.UserProfile;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.DurabilityComponent")
public class DurabilityComponent extends ContentComponent
{
    private float durability;
    private DurabilityProperties min = new DurabilityProperties();
    private DurabilityProperties max = new DurabilityProperties();

    private class DurabilityProperties implements Json.Serializable
    {
        private ObjectMap<String, Float> properties = new ObjectMap<>();

        public float getProperty(String name)
        {
            return properties.get(name, 0.0f);
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            if (jsonData.has("properties"))
            {
                for (JsonValue value : jsonData.get("properties"))
                {
                    properties.put(value.name(), value.asFloat());
                }
            }
        }
    }

    @Override
    public DurabilityComponentData getComponent(ComponentObject componentObject)
    {
        return new DurabilityComponentData(componentObject, this);
    }

    public float getValue(String parameter, float durability)
    {
        return MathUtils.lerp(min.getProperty(parameter), max.getProperty(parameter), durability / this.durability);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.durability = jsonData.getFloat("durability");

        if (jsonData.has("min"))
        {
            min.read(json, jsonData.get("min"));
        }

        if (jsonData.has("max"))
        {
            max.read(json, jsonData.get("max"));
        }
    }

    public float getDurability()
    {
        return durability;
    }

    public float getDurability(UserProfile profile)
    {
        return profile.getStats().get(((Instrument) getContent()).getDurabilityStat(), getDurability());
    }

    public float setDurability(UserProfile profile, float durability)
    {
        return profile.getStats().put(((Instrument) getContent()).getDurabilityStat(), durability);
    }

    public boolean isEnoughtToFix(float durability)
    {
        return getDurability() - durability > 1.0f;
    }
}
