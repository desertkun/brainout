package com.desertkun.brainout.content.components;


import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.RealEstateItem;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.consumable.Resource;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.components.RecipeComponent")
public class RecipeComponent extends ContentComponent
{
    private ObjectMap<String, Integer> requiredItemNames;
    private ObjectMap<Resource, Integer> requiredItems;
    private String requiredStat;

    public RecipeComponent()
    {
        requiredItemNames = new ObjectMap<>();
        requiredItems = new ObjectMap<>();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    public boolean isThereEnough(ObjectMap<Resource, Integer> resources)
    {
        for (ObjectMap.Entry<Resource, Integer> entry : requiredItems)
        {
            if (resources.get(entry.key, 0) < entry.value)
            {
                return false;
            }
        }

        return true;
    }

    public int getMatchingPercentage(ObjectMap<Resource, Integer> resources)
    {
        int totalItems = 0;
        int fulfilledItems = 0;

        for (ObjectMap.Entry<Resource, Integer> entry : requiredItems)
        {
            totalItems += entry.value;
            int have = resources.get(entry.key, 0);
            fulfilledItems += Math.min(have, entry.value);
        }

        return (int)(((float)fulfilledItems / (float)totalItems) * 100.0f);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("items"))
        {
            for (JsonValue entry : jsonData.get("items"))
            {
                requiredItemNames.put(entry.name(), entry.asInt());
            }
        }

        requiredStat = jsonData.getString("stat", null);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        for (ObjectMap.Entry<String, Integer> entry : requiredItemNames)
        {
            requiredItems.put(BrainOut.ContentMgr.get(entry.key, Resource.class), entry.value);
        }
    }

    public ObjectMap<Resource, Integer> getRequiredItems()
    {
        return requiredItems;
    }

    public String getRequiredStat()
    {
        return requiredStat;
    }
}
