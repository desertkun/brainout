package com.desertkun.brainout.content.parallax;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.parallax.LayerData;
import com.desertkun.brainout.data.parallax.ParallaxData;
import com.desertkun.brainout.data.parallax.StaticLayerData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.parallax.StaticLayer")
public class StaticLayer extends Layer
{
    protected TextureAtlas.AtlasRegion texture;
    protected boolean top;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        String textureName = jsonData.getString("texture");
        texture = BrainOutClient.getRegion(textureName);
        top = jsonData.getBoolean("top", false);
    }

    @Override
    public LayerData getData(ParallaxData parallaxData, Map map)
    {
        if (texture == null)
            return null;

        return new StaticLayerData(this, parallaxData, map);
    }

    public boolean isTop()
    {
        return top;
    }

    public TextureAtlas.AtlasRegion getTexture()
    {
        return texture;
    }
}
