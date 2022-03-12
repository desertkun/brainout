package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.ItemsFilters;
import com.desertkun.brainout.utils.StatsFilters;
import com.esotericsoftware.minlog.Log;

@Reflect("content.components.ContentCardComponent")
public class ContentCardComponent extends CardComponent
{
    protected OwnableContent content;
    protected int amount;
    protected boolean limited = true;

    protected StatsFilters statsFilters = null;
    protected ItemsFilters itemsFilters = null;

    @Override
    public boolean applicable(UserProfile profile)
    {
        if (statsFilters != null)
        {
            if (!statsFilters.checkFilters(profile))
            {
                return false;
            }
        }

        if (itemsFilters != null)
        {
            if (!itemsFilters.checkFilters(profile))
            {
                return false;
            }
        }

        if (limited)
            return !content.hasItem(profile);
        
        return true;
    }


    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.content = ((OwnableContent) BrainOut.ContentMgr.get(jsonData.getString("content")));
        this.limited = jsonData.getBoolean("limited", true);

        if (this.content == null)
        {
            System.err.println("Failed to load content for content card " +
                jsonData.toJson(JsonWriter.OutputType.json));
        }

        this.amount = jsonData.getInt("amount", 1);

        if (jsonData.has("stats-filters"))
        {
            statsFilters = new StatsFilters();
            statsFilters.read(json, jsonData.get("stats-filters"));
        }
        
        if (jsonData.has("items-filters"))
        {
            itemsFilters = new ItemsFilters();
            itemsFilters.read(json, jsonData.get("items-filters"));
        }
    }

    public OwnableContent getOwnableContent()
    {
        return this.content;
    }

    public int getAmount()
    {
        return amount;
    }
}
