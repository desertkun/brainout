package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BoosterActivatorComponent")
public class BoosterActivatorComponent extends ContentComponent
{
    public static class BoosterActivator implements Json.Serializable
    {
        public float value;
        public float duration;

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            value = jsonData.getFloat("value", 1);
            duration = jsonData.getFloat("duration", 60);
        }
    }

    private float health;
    private float thirst;
    private float radio;
    private float temp;
    private float hunger;
    private boolean fixBones;
    private ObjectMap<String, BoosterActivator> boosters;

    public BoosterActivatorComponent()
    {
        boosters = new ObjectMap<>();
    }

    public ObjectMap<String, BoosterActivator> getBoosters()
    {
        return boosters;
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    public float getThirst(int quality)
    {
        return Interpolation.sineOut.apply((float)quality / 100.0f) * thirst;
    }

    public float getHealth(int quality)
    {
        return Interpolation.sineOut.apply((float)quality / 100.0f) * health;
    }

    public float getHunger(int quality)
    {
        return Interpolation.sineOut.apply((float)quality / 100.0f) * hunger;
    }

    public float getTemp(int quality)
    {
        return Interpolation.sineOut.apply((float)quality / 100.0f) * temp;
    }

    public boolean isFixBones()
    {
        return fixBones;
    }

    public float getRadio(int quality)
    {
        return Interpolation.sineOut.apply((float)quality / 100.0f) * radio;
    }

    public float getBoosterDuration(float duration, int quality)
    {
        return Interpolation.sineOut.apply((float)quality / 100.0f) * duration;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        JsonValue boosters = jsonData.get("boosters");

        if (boosters != null)
        {
            for (JsonValue b : boosters)
            {
                BoosterActivator a = new BoosterActivator();
                a.read(json, b);

                this.boosters.put(b.name(), a);
            }
        }

        this.thirst = jsonData.getFloat("thirst", 0);
        this.hunger = jsonData.getFloat("hunger", 0);
        this.temp = jsonData.getFloat("temperature", 0);
        this.fixBones = jsonData.getBoolean("fix-bones", false);
        this.health = jsonData.getFloat("health", 0);
        this.radio = jsonData.getFloat("radio", 0);
    }
}
