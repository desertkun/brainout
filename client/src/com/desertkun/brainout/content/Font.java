package com.desertkun.brainout.content;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Font")
public class Font extends Content
{
    protected String fontId;
    protected String fileName;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        fontId = jsonData.getString("id");
        fileName = jsonData.getString("fileName");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        BrainOutClient.FontMgr.registerFont(fontId, assetManager.get(fileName, BitmapFont.class));
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        assetManager.load(fileName, BitmapFont.class);
    }
}
