package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.instrument.Instrument;

public abstract class KillWith extends Task
{
    private String category;
    private Instrument weapon;

    @Override
    protected void readTask(JsonValue jsonData)
    {
        category = jsonData.getString("category", null);
        if (jsonData.has("weapon"))
        {
            weapon = BrainOut.ContentMgr.get(jsonData.getString("weapon"), Instrument.class);
        }
    }

    public String getCategory()
    {
        return category;
    }

    public Instrument getWeapon()
    {
        return weapon;
    }
}
