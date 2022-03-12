package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.GunshotTailSoundEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RandomValue;

@Reflect("content.effect.GunshotTailSoundEffect")
public class GunshotTailSoundEffect extends Effect
{
    private Sound sound;
    private String fileName;
    private boolean distantDelay;
    private RandomValue pitch;
    private boolean loop;
    private float soundDistance;
    private float delayCheck;
    private String key;

    public GunshotTailSoundEffect()
    {
        this.distantDelay = false;
        this.loop = false;
    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new GunshotTailSoundEffectData(this, launchData);
    }

    @Override
    public EffectData getEffect(LaunchData launchData, EffectSet.EffectAttacher attacher)
    {
        return new GunshotTailSoundEffectData(this, launchData, attacher);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        fileName = jsonData.getString("sound");
        key = jsonData.getString("key");
        loop = jsonData.getBoolean("loop", false);
        soundDistance = jsonData.getFloat("distance", 1.0f);

        if (jsonData.has("pitch"))
        {
            this.pitch = new RandomValue(1.0f, 1.0f);
            pitch.read(json, jsonData.get("pitch"));
        }

        distantDelay = jsonData.getBoolean("distantDelay", false);
        delayCheck = jsonData.getFloat("delayCheck", 0.25f);
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

    public String getKey() {
        return key;
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

    public float getDelayCheck()
    {
        return delayCheck;
    }
}
