package com.desertkun.brainout.content.parallax;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.parallax.DynamicLayerData;
import com.desertkun.brainout.data.parallax.LayerData;
import com.desertkun.brainout.data.parallax.ParallaxData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.parallax.DynamicLayer")
public class DynamicLayer extends StaticLayer
{
    private float flowX;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        flowX = jsonData.getFloat("flowX", 0);
    }

    @Override
    public LayerData getData(ParallaxData parallaxData, Map map)
    {
        if (texture == null)
            return null;

        return new DynamicLayerData(this, parallaxData, map);
    }

    public float getFlowX()
    {
        return flowX;
    }
}
