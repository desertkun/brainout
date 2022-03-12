package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.consumable.ConsumableContent;

public abstract class UseItem extends Task
{
    private Array<Content> items = new Array<>();

    @Override
    protected void readTask(JsonValue jsonData)
    {
        if (jsonData.get("items").isArray())
        {
            for (JsonValue value : jsonData.get("items"))
            {
                items.add(BrainOut.ContentMgr.get(value.asString(), Content.class));
            }
        }
        else
        {
            items.add(BrainOut.ContentMgr.get(jsonData.getString("items"), Content.class));
        }
    }

    public Array<Content> getItems()
    {
        return items;
    }
}
