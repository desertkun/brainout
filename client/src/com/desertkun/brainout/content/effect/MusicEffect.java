package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.MusicEffectData;
import com.desertkun.brainout.data.effect.SoundEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.MusicEffect")
public class MusicEffect extends Effect
{
    private Music sound;
    private String fileName;
    private boolean loop;
    private float soundDistance;

    public MusicEffect()
    {
        this.loop = false;
    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new MusicEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        fileName = jsonData.getString("music");
        loop = jsonData.getBoolean("loop", false);
        soundDistance = jsonData.getFloat("distance", 1.0f);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        sound = assetManager.get(fileName, Music.class);
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        assetManager.load(fileName, Music.class);
    }

    public Music getMusic()
    {
        return sound;
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
