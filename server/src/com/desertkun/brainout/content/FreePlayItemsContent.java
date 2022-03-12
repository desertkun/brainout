package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.mode.freeplay.FreeplayItems;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import org.json.JSONArray;

@Reflect("content.FreePlayItems")
public class FreePlayItemsContent extends Content
{
    private FreeplayItems items;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        items = new FreeplayItems(new JSONArray(jsonData.get("items").toJson(JsonWriter.OutputType.json)));
    }

    public FreeplayItems getItems()
    {
        return items;
    }

    public static FreeplayItems Get()
    {
        FreePlayItemsContent items = BrainOutServer.ContentMgr.get("freeplay-items-spawm", FreePlayItemsContent.class);
        if (items == null)
        {
            return null;
        }

        return items.getItems();
    }
}
