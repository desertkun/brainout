package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.MusicList")
public class MusicList extends Content
{
    private Array<Music> musicArray;

    public MusicList()
    {
        musicArray = new Array<Music>();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("list"))
        {
            JsonValue list = jsonData.get("list");

            if (list.isArray())
            {
                for (JsonValue i: list)
                {
                    Music music = ((Music) BrainOut.ContentMgr.get(i.asString()));

                    if (music != null)
                    {
                        musicArray.add(music);
                    }
                }
            }
        }
    }

    public Array<Music> getMusic()
    {
        return musicArray;
    }

    public Music getRandom()
    {
        return musicArray.random();
    }
}
