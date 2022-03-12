package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.StartStopSoundEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RandomValue;

@Reflect("content.effect.StartStopSoundEffect")
public class StartStopSoundEffect extends Effect
{
    private float soundDistance;
    private float startTime;
    private float stopTime;

    private Sound start;
    private Sound loop;
    private Sound stop;

    private String startFileName;
    private String loopFileName;
    private String stopFileName;
    private RandomValue pitch;

    public StartStopSoundEffect()
    {

    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new StartStopSoundEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        soundDistance = jsonData.getFloat("distance", 1.0f);

        startTime = jsonData.getFloat("startTime");
        stopTime = jsonData.getFloat("stopTime");

        startFileName = jsonData.getString("start");
        loopFileName = jsonData.getString("loop");
        stopFileName = jsonData.getString("stop");

        if (jsonData.has("pitch"))
        {
            this.pitch = new RandomValue(1.0f, 1.0f);
            pitch.read(json, jsonData.get("pitch"));
        }
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        start = assetManager.get(startFileName, Sound.class);
        loop = assetManager.get(loopFileName, Sound.class);
        stop = assetManager.get(stopFileName, Sound.class);
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        assetManager.load(startFileName, Sound.class);
        assetManager.load(loopFileName, Sound.class);
        assetManager.load(stopFileName, Sound.class);
    }

    public float getStartTime()
    {
        return startTime;
    }

    public float getStopTime()
    {
        return stopTime;
    }

    public float getSoundDistance()
    {
        return soundDistance;
    }

    public Sound getStartSound()
    {
        return start;
    }

    public Sound getLoopSound()
    {
        return loop;
    }

    public Sound getStopSound()
    {
        return stop;
    }

    public RandomValue getPitch()
    {
        return pitch;
    }
}
