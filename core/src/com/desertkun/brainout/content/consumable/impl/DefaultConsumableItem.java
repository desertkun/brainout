package com.desertkun.brainout.content.consumable.impl;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("defconsit")
@ReflectAlias("content.consumable.impl.DefaultConsumableItem")
public class DefaultConsumableItem extends ConsumableItem
{
    private ConsumableContent consumableContent;

    public DefaultConsumableItem(ConsumableContent consumableContent)
    {
        this.consumableContent = consumableContent;
    }

    public DefaultConsumableItem()
    {
        this.consumableContent = null;
    }

    @Override
    public ConsumableContent getContent()
    {
        return consumableContent;
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        if (!getContent().isStacks())
            return false;

        if (item.getContent() instanceof ConsumableContent && !((ConsumableContent) item.getContent()).isStacks())
            return false;

        if (isPrivate() && getPrivate() != item.getPrivate())
            return false;

        return item instanceof DefaultConsumableItem &&
            ((DefaultConsumableItem) item).getContent() == consumableContent;
    }

    public ConsumableContent getConsumableContent()
    {
        return consumableContent;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("content"))
        {
            JsonValue c = jsonData.get("content");
            consumableContent = ((ConsumableContent) BrainOut.ContentMgr.get(c.asString()));
        }
        else
        {
            consumableContent = null;
        }
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        if (consumableContent != null)
        {
            json.writeValue("content", consumableContent.getID());
        }
    }
}
