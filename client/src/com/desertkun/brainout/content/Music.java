package com.desertkun.brainout.content;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Music")
public class Music extends Content
{
    private String musicName;
    private float length;
    private com.badlogic.gdx.audio.Music music;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        musicName = jsonData.getString("music");
        length = jsonData.getFloat("length", 0);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        try
        {
            FileHandle musicFile = BrainOut.PackageMgr.getFile(musicName);

            if (musicFile == null)
                return;

            this.music = Gdx.audio.newMusic(musicFile);
        }
        catch (GdxRuntimeException ignored)
        {
            //
        }
    }

    public com.badlogic.gdx.audio.Music getMusic()
    {
        return music;
    }

    public float getLength()
    {
        return length;
    }

    public void playInSoundChannel()
    {
        if (music == null)
            return;

        try
        {
            music.setVolume(BrainOutClient.ClientSett.getSoundVolume().getFloatValue());
            music.play();
        }
        catch (GdxRuntimeException ignored)
        {
            //
        }
    }
}
