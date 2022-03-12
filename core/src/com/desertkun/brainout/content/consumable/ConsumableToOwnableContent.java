package com.desertkun.brainout.content.consumable;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.ConsumableToOwnableContent")
public class ConsumableToOwnableContent extends ConsumableContent
{
    private OwnableContent ownableContent;
    private String ownableContentId;
    private int weight;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        ownableContentId = jsonData.getString("ownable");
        weight = jsonData.getInt("weight", 1);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        ownableContent = BrainOut.ContentMgr.get(ownableContentId, OwnableContent.class);
    }

    public OwnableContent getOwnableContent()
    {
        return ownableContent;
    }

    public int getWeight()
    {
        return weight;
    }
}
