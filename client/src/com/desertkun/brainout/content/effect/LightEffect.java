package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.LightEntity;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.LightEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.LightEffect")
public class LightEffect extends Effect
{
    private Color colorBefore;
    private Color colorAfter;
    private float timeBefore;
    private float timeAfter;
    private LightEntity lightEntity;
    private float time;

    public LightEffect()
    {
        lightEntity = new LightEntity(false);
    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new LightEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        lightEntity.read(json, jsonData);

        this.colorBefore = Color.valueOf(jsonData.getString("colorBefore", "00000000"));
        this.colorAfter = Color.valueOf(jsonData.getString("colorAfter", "00000000"));
        this.timeBefore = jsonData.getFloat("timeBefore", 0.2f);
        this.timeAfter = jsonData.getFloat("timeAfter", 0.2f);
        this.time = jsonData.getFloat("time", 1.0f);
    }

    public Color getColorBefore()
    {
        return colorBefore;
    }

    public Color getColorAfter()
    {
        return colorAfter;
    }

    public float getTimeBefore()
    {
        return timeBefore;
    }

    public float getTimeAfter()
    {
        return timeAfter;
    }

    public float getTime()
    {
        return time;
    }

    public LightEntity getLightEntity()
    {
        return lightEntity;
    }
}
