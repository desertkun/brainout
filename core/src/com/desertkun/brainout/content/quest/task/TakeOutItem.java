package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.quest.task.Task;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

public abstract class TakeOutItem extends Task
{
    private ConsumableContent item;

    @Override
    protected void readTask(JsonValue jsonData)
    {
        String itemName = jsonData.getString("item");

        item = BrainOut.ContentMgr.get(itemName, ConsumableContent.class);
    }

    public ConsumableContent getItem()
    {
        return item;
    }
}
