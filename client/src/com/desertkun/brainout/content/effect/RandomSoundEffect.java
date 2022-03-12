package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.RandomSoundEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RandomValue;

@Reflect("content.effect.RandomSoundEffect")
public class RandomSoundEffect extends Effect
{
    private Array<Sound> sounds;
    private Array<String> fileNames;
    private boolean distantDelay;
    private RandomValue pitch;
    private boolean loop;
    private float soundDistance;
    private float minimumDistance;

    public RandomSoundEffect()
    {
        this.fileNames = new Array<>();
        this.sounds = new Array<>();
        this.distantDelay = false;
        this.loop = false;
    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new RandomSoundEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        for (JsonValue value : jsonData.get("sounds"))
        {
            fileNames.add(value.asString());
        }

        loop = jsonData.getBoolean("loop", false);
        soundDistance = jsonData.getFloat("distance", 1.0f);
        minimumDistance = jsonData.getFloat("minimumDistance", 0f);

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

        for (String fileName : fileNames)
        {
            sounds.add(assetManager.get(fileName, Sound.class));
        }
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        for (String fileName : fileNames)
        {
            assetManager.load(fileName, Sound.class);
        }
    }

    public Array<Sound> getSounds()
    {
        return sounds;
    }

    public boolean isDistantDelay()
    {
        return distantDelay;
    }

    public float getMinimumDistance()
    {
        return minimumDistance;
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
