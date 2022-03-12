package com.desertkun.brainout.content.consumable.impl;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.consumable.Armor;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.events.CollideEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.impl.ArmorConsumableItem")
public class ArmorConsumableItem extends ConsumableItem
{
    private Armor armor;
    private final ObjectMap<String, Float> protect;

    public ArmorConsumableItem(Armor armor)
    {
        this.armor = armor;
        this.protect = new ObjectMap<>(armor.getProtect());
    }

    public ArmorConsumableItem()
    {
        this.armor = null;
        this.protect = new ObjectMap<>();
    }

    public ObjectMap<String, Float> getProtect()
    {
        return protect;
    }

    public float getProtect(String key, int quality)
    {
        if (quality == -1)
        {
            return protect.get(key, 0.f);
        }

        return protect.get(key, 0.f) * ((float)quality / 100.0f);
    }

    public float protect(CollideEvent event, float damage, int quality)
    {
        float p = getProtect(event.colliderName, quality);

        if (p == 0)
        {
            return damage;
        }

        float result = p - damage;
        protect.put(event.colliderName, result);

        if (result > 0)
        {
            return 0;
        }
        else
        {
            return -result;
        }
    }

    public boolean empty()
    {
        for (ObjectMap.Entry<String, Float> entry : protect)
        {
            if (entry.value > 0)
                return false;
        }

        return true;
    }

    @Override
    public ConsumableContent getContent()
    {
        return armor;
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        if (armor != null)
        {
            json.writeValue("ar", armor.getID());
        }

        json.writeObjectStart("p");
        for (ObjectMap.Entry<String, Float> entry : protect)
        {
            json.writeValue(entry.key, entry.value);
        }
        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("ar"))
        {
            String id_ = jsonData.getString("ar");
            armor = BrainOut.ContentMgr.get(id_, Armor.class);
        }
        else
        {
            armor = null;
        }

        if (jsonData.has("p"))
        {
            protect.clear();

            for (JsonValue p : jsonData.get("p"))
            {
                protect.put(p.name(), p.asFloat());
            }
        }
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        return false;
    }
}
