package com.desertkun.brainout.content.parallax;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.parallax.ParallaxData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.parallax.Parallax")
public class Parallax extends Content
{
    private Array<Layer> layers;

    public Parallax()
    {

    }

    public ParallaxData getData(Map map)
    {
        return new ParallaxData(this, map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        layers = json.readValue(Array.class, Layer.class, jsonData.get("layers"));
    }

    public Array<Layer> getLayers()
    {
        return layers;
    }
}
