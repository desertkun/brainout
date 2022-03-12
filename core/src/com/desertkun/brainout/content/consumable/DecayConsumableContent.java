package com.desertkun.brainout.content.consumable;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.consumable.impl.DecayConsumableItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.DecayConsumableContent")
public class DecayConsumableContent extends ConsumableContent
{
    private int uses;

    public DecayConsumableContent()
    {
    }

    @Override
    public DecayConsumableItem acquireConsumableItem()
    {
        return new DecayConsumableItem(this);
    }

    public int getUses()
    {
        return uses;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        uses = jsonData.getInt("uses");
    }
}
