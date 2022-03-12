package com.desertkun.brainout.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class ValueRange implements Json.Serializable
{
    private float min;
    private float max;

    public ValueRange()
    {
        this.min = 0;
        this.max = 0;
    }


    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData == null) return;

        if (jsonData.isObject())
        {
            float min = jsonData.getFloat("min");
            float max = jsonData.getFloat("max");

            this.min = min;
            this.max = max;
        }

        if (jsonData.isNumber())
        {
            this.min = jsonData.asFloat();
            this.max = this.min;
        }

    }

    public float getMin()
    {
        return min;
    }

    public float getMax()
    {
        return max;
    }

    public float getLength()
    {
        return max - min;
    }

    public boolean isInRange(float value)
    {
        return value >= min && value <= max;
    }

    public float getRangeCoef(float value)
    {
        return (MathUtils.clamp(value, min, max) - min) / getLength();
    }

    public float getValue(float coef)
    {
        return min + (MathUtils.clamp(coef, 0, 1) * getLength());
    }
}