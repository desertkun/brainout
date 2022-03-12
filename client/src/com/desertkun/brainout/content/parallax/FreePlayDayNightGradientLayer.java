package com.desertkun.brainout.content.parallax;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.parallax.FreePlayDayNightGradientLayerData;
import com.desertkun.brainout.data.parallax.LayerData;
import com.desertkun.brainout.data.parallax.ParallaxData;
import com.desertkun.brainout.menu.ui.CharacteristicsPanel;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.parallax.FreePlayDayNightGradientLayer")
public class FreePlayDayNightGradientLayer extends Layer
{
    protected Array<TextureAtlas.AtlasRegion> textures;

    protected int[] timeWeights;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        textures = new Array<>();

        for (String txt : jsonData.get("textures").asStringArray())
        {
            textures.add(BrainOutClient.getRegion(txt));
        }

        timeWeights = jsonData.get("timeWeight").asIntArray();
    }

    @Override
    public LayerData getData(ParallaxData parallaxData, Map map)
    {
        if (textures.size < 2)
            return null;

        if (textures.get(0) == null)
            return null;

        return new FreePlayDayNightGradientLayerData(this, parallaxData, map);
    }

    public Array<TextureAtlas.AtlasRegion> getTextures()
    {
        return textures;
    }

    public int[] getTimeWeights() {
        return timeWeights;
    }
}
