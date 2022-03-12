package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.MusicEffectData;
import com.desertkun.brainout.data.effect.MusicListEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.MusicListEffect")
public class MusicListEffect extends Effect
{
    private Array<Music> sounds;
    private Array<String> fileNames;
    private float soundDistance;

    public MusicListEffect()
    {
        this.fileNames = new Array<>();
        this.sounds = new Array<>();
    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new MusicListEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        for (JsonValue music : jsonData.get("music"))
        {
            fileNames.add(music.asString());
        }

        soundDistance = jsonData.getFloat("distance", 1.0f);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        for (String fileName : fileNames)
        {
            sounds.add(assetManager.get(fileName, Music.class));
        }
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        for (String fileName : fileNames)
        {
            assetManager.load(fileName, Music.class);
        }
    }

    public Array<Music> getMusic()
    {
        return sounds;
    }

    public float getSoundDistance()
    {
        return soundDistance;
    }
}
