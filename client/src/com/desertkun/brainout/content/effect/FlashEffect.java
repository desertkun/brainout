package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.FlashEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.FlashEffect")
public class FlashEffect extends Effect
{
    private float duration;

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new FlashEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        duration = jsonData.getFloat("duration");
    }

    public float getDuration()
    {
        return duration;
    }
}
