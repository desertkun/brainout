package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.BackgroundSoundEffectData;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.BackgroundSoundEffect")
public class BackgroundSoundEffect extends Effect
{
    private Sound sound;
    private String fileName;

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new BackgroundSoundEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        fileName = jsonData.getString("sound");
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
}
