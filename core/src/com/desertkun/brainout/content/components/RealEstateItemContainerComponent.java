package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.components.RealEstateItemContainerComponent")
public class RealEstateItemContainerComponent extends ContentComponent
{
    private int itemLimit;
    private int weightLimit;
    private String tagLimit;

    public static class Placeholder
    {
        public String placeholder;
        public int amount;
    }

    private Array<Placeholder> placeholders;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        itemLimit = jsonData.getInt("itemLimit", 0);
        weightLimit = jsonData.getInt("weightLimit", 0);
        tagLimit = jsonData.getString("tagLimit", null);

        if (jsonData.has("placeholders"))
        {
            placeholders = new Array<>();

            for (JsonValue entry : jsonData.get("placeholders"))
            {
                Placeholder p = new Placeholder();
                p.placeholder = entry.getString("placeholder");
                p.amount = entry.getInt("amount");
                placeholders.add(p);
            }
        }
    }

    public Array<Placeholder> getPlaceholders()
    {
        return placeholders;
    }

    public String getTagLimit()
    {
        return tagLimit;
    }

    public int getItemLimit()
    {
        return itemLimit;
    }

    public int getWeightLimit()
    {
        return weightLimit;
    }
}
