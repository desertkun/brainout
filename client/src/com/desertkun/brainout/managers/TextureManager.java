package com.desertkun.brainout.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;

public class TextureManager
{
	private ArrayMap<String, TextureAtlas> data;
	private Array<String> dataNames;

	private TextureAtlas game;
	
	public void loadAtlas(String fileName, AssetManager assetManager)
	{
        assetManager.load(fileName, TextureAtlas.class);
	}

    public TextureAtlas getGameAtlas()
    {
        return game;
    }

    public void registerAtlas(String atlasName, String fileName, AssetManager assetManager)
    {
        dataNames.add(atlasName);
        TextureAtlas txt;

        try
        {
            txt = assetManager.get(fileName, TextureAtlas.class);
        }
        catch (GdxRuntimeException ignored)
        {
            return;
        }

        if (atlasName.equals("GAME"))
        {
            game = txt;
        }

        data.put(atlasName, txt);

        BrainOutClient.Skin.addRegions(txt);
    }

    public void cleanupAtlas(String atlasName)
    {
        TextureAtlas txt = data.removeKey(atlasName);

        if (txt == null)
            return;

        for (TextureAtlas.AtlasRegion region : txt.getRegions())
        {
            String name = region.name;

            if (region.index != -1) {
                name += "_" + region.index;
            }

            BrainOutClient.Skin.remove(name, TextureRegion.class);
        }
    }

    public TextureAtlas getAtlas(String id)
    {
        return data.get(id);
    }

	public TextureManager()
	{
		data = new ArrayMap<String, TextureAtlas>();
		dataNames = new Array<String>();
	}

    public void loadAtlases(JsonValue jsonData, AssetManager assetManager)
    {
        if (jsonData.isObject())
        {
            for (JsonValue value: jsonData)
            {
                if (value.isString())
                {
                    loadAtlas(value.asString(), assetManager);
                }
            }
        }
    }

    public void registerAtlases(JsonValue jsonData, AssetManager assetManager)
    {
        if (jsonData.isObject())
        {
            for (JsonValue value: jsonData)
            {
                if (value.isString())
                {
                    registerAtlas(value.name, value.asString(), assetManager);
                }
            }
        }
    }
}
