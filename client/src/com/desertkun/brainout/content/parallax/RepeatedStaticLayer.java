package com.desertkun.brainout.content.parallax;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.parallax.LayerData;
import com.desertkun.brainout.data.parallax.ParallaxData;
import com.desertkun.brainout.data.parallax.RepeatedStaticLayerData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.parallax.RepeatedStaticLayer")
public class RepeatedStaticLayer extends StaticLayer
{
    private float repeatY;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        repeatY = jsonData.getFloat("repeatY", 0);
    }

    public float getRepeatY()
    {
        return repeatY;
    }

    @Override
    public LayerData getData(ParallaxData parallaxData, Map map)
    {
        if (texture == null)
            return null;

        return new RepeatedStaticLayerData(this, parallaxData, map);
    }
}
