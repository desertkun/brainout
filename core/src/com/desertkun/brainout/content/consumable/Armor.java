package com.desertkun.brainout.content.consumable;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.consumable.impl.ArmorConsumableItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.Armor")
public class Armor extends ConsumableContent
{
    private ObjectMap<String, Float> protect;

    public Armor()
    {
        protect = new ObjectMap<>();
    }

    @Override
    public ArmorConsumableItem acquireConsumableItem()
    {
        return new ArmorConsumableItem(this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("protect"))
        {
            for (JsonValue value : jsonData.get("protect"))
            {
                protect.put(value.name(), value.asFloat());
            }
        }
    }

    public ObjectMap<String, Float> getProtect()
    {
        return protect;
    }
}
