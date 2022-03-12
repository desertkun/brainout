package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.JsonValue;

public class ActivateItem extends Task
{
    private String item;

    @Override
    protected void readTask(JsonValue jsonData)
    {
        item = jsonData.getString("item");
    }

    public String getItem()
    {
        return item;
    }
}
