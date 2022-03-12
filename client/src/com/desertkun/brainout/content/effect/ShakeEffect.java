package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.ShakeEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.ShakeEffect")
public class ShakeEffect extends Effect
{
    private float distance;
    private float power;

    @Override
    public ShakeEffectData getEffect(LaunchData launchData)
    {
        return new ShakeEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.distance = jsonData.getFloat("distance", 0);
        this.power = jsonData.getFloat("power", 0);
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
