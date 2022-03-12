package com.desertkun.brainout.content;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Sound")
public class Sound extends Content
{
    private String fileName;
    private com.badlogic.gdx.audio.Sound sound;

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

        sound = assetManager.get(fileName, com.badlogic.gdx.audio.Sound.class);
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        assetManager.load(fileName, com.badlogic.gdx.audio.Sound.class);
    }

    public long play()
    {
        if (sound == null)
        {
            return -1;
        }

        return sound.play(BrainOutClient.ClientSett.getSoundVolume().getFloatValue());
    }

    public com.badlogic.gdx.audio.Sound getSound()
    {
        return sound;
    }
}
