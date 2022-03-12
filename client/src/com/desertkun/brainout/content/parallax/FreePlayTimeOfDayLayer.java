package com.desertkun.brainout.content.parallax;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.parallax.FreePlayDayNightGradientLayerData;
import com.desertkun.brainout.data.parallax.FreePlayTimeOfDayLayerData;
import com.desertkun.brainout.data.parallax.LayerData;
import com.desertkun.brainout.data.parallax.ParallaxData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.parallax.FreePlayTimeOfDayLayer")
public class FreePlayTimeOfDayLayer extends DynamicLayer
{
    protected float timeOfDay;
    protected float timeLength;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        timeOfDay = jsonData.getFloat("timeOfDay");
        timeLength = jsonData.getFloat("timeLength");
    }

    @Override
    public LayerData getData(ParallaxData parallaxData, Map map)
    {
        if (texture == null)
            return null;

        return new FreePlayTimeOfDayLayerData(this, parallaxData, map);
    }

    public float getTimeOfDay()
    {
        return timeOfDay;
    }

    public float getTimeLength()
    {
        return timeLength;
    }
}
