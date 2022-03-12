package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;

public class BurnItem extends Task
{
    private Array<Content> items = new Array<>();

    @Override
    protected void readTask(JsonValue jsonData)
    {
        if (jsonData.has("item"))
        {
            items.add(BrainOut.ContentMgr.get(jsonData.getString("item"), Content.class));
        }
        else if (jsonData.has("items"))
        {
            for (JsonValue ite : jsonData.get("items"))
            {
                items.add(BrainOut.ContentMgr.get(ite.asString(), Content.class));
            }
        }
    }

    public Array<Content> getItems()
    {
        return items;
    }
}
