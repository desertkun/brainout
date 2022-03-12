package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.DropComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.DropComponent")
public class DropComponent extends ContentComponent
{
    private Item dropItem;
    private ObjectMap<String, Float> chances;

    public DropComponent()
    {
        dropItem = null;
        chances = new ObjectMap<String, Float>();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new DropComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {
        //
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.dropItem = (Item)BrainOut.ContentMgr.get(jsonData.getString("dropItem"));

        if (jsonData.has("chances") && jsonData.get("chances").isObject())
        {
            for (JsonValue chance: jsonData.get("chances"))
            {
                chances.put(chance.name(), chance.asFloat());
            }
        }
    }

    public Item getDropItem()
    {
        return dropItem;
    }

    public boolean hasChance(String itemName)
    {
        return chances.get(itemName) != null;
    }

    public boolean checkChance(String itemName)
    {
        Float chance = chances.get(itemName);

        if (chance == null)
        {
            chance = chances.get("*");
        }

        return chance != null && chance != 0 && Math.random() <= chance;

    }
}
