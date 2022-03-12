package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.effect.EffectData;

public abstract class Effect extends Content
{
    private boolean enabled;

    public abstract EffectData getEffect(LaunchData launchData);

    public EffectData getEffect(LaunchData launchData, EffectSet.EffectAttacher attacher)
    {
        return getEffect(launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        enabled = jsonData.getBoolean("enabled", true);
    }

    public boolean isEnabled()
    {
        return enabled;
    }
}
