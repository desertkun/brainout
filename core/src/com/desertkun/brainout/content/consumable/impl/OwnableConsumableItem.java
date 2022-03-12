package com.desertkun.brainout.content.consumable.impl;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.impl.OwnableConsumableItem")
public class OwnableConsumableItem extends ConsumableItem
{
    private OwnableContent ownableContent;

    public OwnableConsumableItem(OwnableContent ownableContent)
    {
        this.ownableContent = ownableContent;
    }

    public OwnableConsumableItem()
    {
        this.ownableContent = null;
    }

    @Override
    public OwnableContent getContent()
    {
        return ownableContent;
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        if (isPrivate() && getPrivate() != item.getPrivate())
            return false;

        return item instanceof OwnableConsumableItem && ((OwnableConsumableItem) item).getContent() == ownableContent;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        ownableContent = ((OwnableContent) BrainOut.ContentMgr.get(jsonData.getString("content")));
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("content", ownableContent.getID());
    }
}
