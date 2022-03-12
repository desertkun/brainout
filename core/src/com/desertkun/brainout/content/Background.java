package com.desertkun.brainout.content;


import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Background")
public class Background extends Content
{
    private String musicList;
    private String parallax;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        musicList = jsonData.getString("music-list");
        parallax = jsonData.getString("parallax");
    }

    public String getMusicList()
    {
        return musicList;
    }

    public String getParallax()
    {
        return parallax;
    }

    public void activate(Map map)
    {
        map.setCustom("parallax", getParallax());
        map.setCustom("music-list", getMusicList());
    }
}
