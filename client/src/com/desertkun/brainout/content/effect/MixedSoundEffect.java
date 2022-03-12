package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.MixedSoundEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.MixedSoundEffect")
public class MixedSoundEffect extends SoundEffect
{
    private String distantFileName;
    private Sound distantSound;

    private float distantChangeFrom;
    private float distantChangeTo;

    @Override
    public MixedSoundEffectData getEffect(LaunchData launchData)
    {
        return new MixedSoundEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        distantFileName = jsonData.getString("distantSound");
        distantChangeFrom = jsonData.getFloat("changeFrom");
        distantChangeTo = jsonData.getFloat("changeTo");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        distantSound = assetManager.get(distantFileName, Sound.class);
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        assetManager.load(distantFileName, Sound.class);
    }

    public Sound getDistantSound()
    {
        return distantSound;
    }

    public float getDistantChangeFrom()
    {
        return distantChangeFrom;
    }

    public float getDistantChangeTo()
    {
        return distantChangeTo;
    }
}
