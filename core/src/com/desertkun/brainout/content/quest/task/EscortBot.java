package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.utils.LocalizedString;

public abstract class EscortBot extends Task
{
    private LocalizedString name;
    private LocalizedString location;
    private Array<String> relatedItems;

    @Override
    protected void readTask(JsonValue jsonData)
    {
        name = new LocalizedString(jsonData.getString("name"));
        location = new LocalizedString(jsonData.getString("location"));

        relatedItems = new Array<>();

        if (jsonData.has("related-items"))
        {
            for (JsonValue value : jsonData.get("related-items"))
            {
                relatedItems.add(value.asString());
            }
        }
    }

    public LocalizedString getName()
    {
        return name;
    }

    public LocalizedString getLocation()
    {
        return location;
    }

    public Array<String> getRelatedItems()
    {
        return relatedItems;
    }
}
