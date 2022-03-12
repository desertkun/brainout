package com.desertkun.brainout.content.parallax;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.parallax.LayerData;
import com.desertkun.brainout.data.parallax.ParallaxData;

public abstract class Layer implements Json.Serializable
{
    private float y;
    private float x;
    private float coefX;
    private float coefY;
    private boolean scale;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        x = jsonData.getFloat("x");
        y = jsonData.getFloat("y");
        coefX = jsonData.getFloat("coefX");
        coefY = jsonData.getFloat("coefY");
        scale = jsonData.getBoolean("scale", false);
    }

    @Override
    public void write(Json json)
    {
        //
    }

    public void render(SpriteBatch batch)
    {
        //
    }

    public abstract LayerData getData(ParallaxData parallaxData, Map map);

    public float getY()
    {
        return y;
    }

    public float getX()
    {
        return x;
    }

    public float getCoefX()
    {
        return coefX;
    }

    public float getCoefY()
    {
        return coefY;
    }

    public boolean isScale()
    {
        return scale;
    }
}
