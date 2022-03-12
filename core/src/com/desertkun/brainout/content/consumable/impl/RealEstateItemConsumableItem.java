package com.desertkun.brainout.content.consumable.impl;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.active.RealEstateItem;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.RealEstateContent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("RealEstateItemConsumableItem")
@ReflectAlias("content.consumable.impl.RealEstateItemConsumableItem")
public class RealEstateItemConsumableItem extends ConsumableItem
{
    private final RealEstateItem item;
    private String kind;

    public RealEstateItemConsumableItem(RealEstateItem item)
    {
        this.item = item;
        this.kind = item.getKind();
    }

    @Override
    public RealEstateItem getContent()
    {
        return item;
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        return (item instanceof RealEstateItemConsumableItem) &&
            (((RealEstateItemConsumableItem) item).item == this.item);
    }

    public RealEstateItem getItem()
    {
        return item;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        kind = jsonData.getString("k", kind);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("k", kind);
    }
}
