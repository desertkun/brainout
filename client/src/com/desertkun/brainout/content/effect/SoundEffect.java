package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.SoundEffectData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RandomValue;

@Reflect("content.effect.SoundEffect")
public class SoundEffect extends Effect
{
    private Sound sound;
    private String fileName;
    private boolean distantDelay;
    private RandomValue pitch;
    private boolean loop;
    private float soundDistance;

    public SoundEffect()
    {
        this.distantDelay = false;
        this.loop = false;
    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new SoundEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        fileName = jsonData.getString("sound");
        loop = jsonData.getBoolean("loop", false);
        soundDistance = jsonData.getFloat("distance", 1.0f);

        if (jsonData.has("pitch"))
        {
            if (jsonData.get("pitch").isNumber())
            {
                float f = jsonData.getFloat("pitch");
                this.pitch = new RandomValue(f, f);
            }
            else
            {
                this.pitch = new RandomValue(1.0f, 1.0f);
                pitch.read(json, jsonData.get("pitch"));
            }
        }

        distantDelay = jsonData.getBoolean("distantDelay", false);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        sound = assetManager.get(fileName, Sound.class);
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        assetManager.load(fileName, Sound.class);
    }

    public Sound getSound()
    {
        return sound;
    }

    public boolean isDistantDelay()
    {
        return distantDelay;
    }

    public RandomValue getPitch()
    {
        return pitch;
    }

    public boolean isLoop()
    {
        return loop;
    }

    public float getSoundDistance()
    {
        return soundDistance;
    }
}
