package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.RandomValue")
public class RandomValue implements Json.Serializable
{
    private float value;
    private float valueDiff;

    public RandomValue(float defaultMin, float defaultMax)
    {
        this.value = defaultMin;
        this.valueDiff = defaultMax - defaultMin;
    }

    public RandomValue()
    {
        this(1.0f, 1.0f);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData == null) return;

        if (jsonData.isObject()) {
            float min = jsonData.getFloat("min");
            float max = jsonData.getFloat("max");

            this.value = min;
            this.valueDiff = max - min;
        }

        if (jsonData.isNumber())
        {
            this.value = jsonData.asFloat();
        }

    }

    public float getValue()
    {
        return this.value + this.valueDiff * (float)Math.random();
    }
}
