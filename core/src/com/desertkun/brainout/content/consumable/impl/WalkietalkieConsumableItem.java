package com.desertkun.brainout.content.consumable.impl;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.Walkietalkie;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.consumable.WalkietalkieConsumableItem")
public class WalkietalkieConsumableItem extends ConsumableItem
{
    private int frequency;
    private Walkietalkie content;

    public WalkietalkieConsumableItem()
    {
        this.content = null;
    }

    public WalkietalkieConsumableItem(Walkietalkie content)
    {
        this.content = content;
        this.frequency = Walkietalkie.getRandomFrequency();
    }

    public void setFrequency(int frequency)
    {
        this.frequency = Walkietalkie.validateFrequency(frequency);
    }

    public int getFrequency()
    {
        return frequency;
    }

    @Override
    public Content getContent()
    {
        return content;
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        return false;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("content"))
        {
            content = ((Walkietalkie) BrainOut.ContentMgr.get(jsonData.getString("content")));
        }
        else
        {
            content = null;
        }

        frequency = jsonData.getInt("frequency");
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        if (content != null)
            json.writeValue("content", content.getID());

        json.writeValue("frequency", frequency);
    }
}
