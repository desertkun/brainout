package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.JsonValue;

public abstract class PaintItemOfCategory extends Task
{
    private String category;

    @Override
    protected void readTask(JsonValue jsonData)
    {
        category = jsonData.getString("category");
    }

    public String getCategory()
    {
        return category;
    }
}
