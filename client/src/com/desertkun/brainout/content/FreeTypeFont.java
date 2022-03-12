package com.desertkun.brainout.content;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.FreeTypeFont")
public class FreeTypeFont extends Font
{
    private int size;
    private FreeTypeFontGenerator generator;

    private int shadowOffsetX, shadowOffsetY;
    private String shadowColor;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        size = jsonData.getInt("size", 20);

        shadowOffsetX = jsonData.getInt("shadowOffsetX", 0);
        shadowOffsetY = jsonData.getInt("shadowOffsetY", 0);
        shadowColor = jsonData.getString("shadowColor", null);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        generator = new FreeTypeFontGenerator(BrainOutClient.PackageMgr.getFile(fileName));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = size;
        parameter.incremental = true;

        if (shadowColor != null)
        {
            parameter.shadowColor = Color.valueOf(shadowColor);
            parameter.shadowOffsetX = shadowOffsetX;
            parameter.shadowOffsetY = shadowOffsetY;
        }

        BitmapFont font = generator.generateFont(parameter);
        BrainOutClient.FontMgr.registerFont(fontId, font);
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        //
    }
}
