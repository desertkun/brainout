package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.FlashbangEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.FlashbangEffect")
public class FlashbangEffect extends Effect
{
    private float flashDuration;
    private float fadeOutDuration;

    @Override
    public FlashbangEffectData getEffect(LaunchData launchData)
    {
        return new FlashbangEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.flashDuration = jsonData.getFloat("flashDuration");
        this.fadeOutDuration = jsonData.getFloat("fadeOutDuration");
    }

    public float getFadeOutDuration()
    {
        return fadeOutDuration;
    }

    public float getFlashDuration()
    {
        return flashDuration;
    }
}
