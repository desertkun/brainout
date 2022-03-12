package com.desertkun.brainout.data.consumable;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.data.Map;

public class ConsumableRecord implements Json.Serializable
{
    private ConsumableItem item;
    private String tag;
    private int amount;
    private int id;
    private int who;
    private int quality;

    public ConsumableRecord(ConsumableItem item, int amount, int id)
    {
        this.item = item;
        this.amount = amount;
        this.id = id;
        this.who = -1;
        this.quality = -1;
    }

    public ConsumableRecord()
    {

    }

    public boolean hasQuality()
    {
        return quality >= 0;
    }

    public int getQuality()
    {
        return quality;
    }

    public void setQuality(int quality)
    {
        this.quality = quality;
    }

    public boolean isGoodQuality()
    {
        return quality >= 80;
    }

    public boolean isBadQuality()
    {
        return quality <= 20;
    }

    public boolean isTrash()
    {
        return hasQuality() && quality == 0;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public String getTag()
    {
        return tag;
    }

    public ConsumableItem getItem()
    {
        return item;
    }

    public int getAmount()
    {
        return amount;
    }

    public boolean isValid()
    {
        return item != null && item.isValid();
    }

    public void setItem(ConsumableItem item)
    {
        this.item = item;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }

    public int getId()
    {
        return id;
    }

    public void setWho(int who)
    {
        this.who = who;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("amount", amount);
        if (item != null)
        {
            json.writeValue("item", item);
        }
        if (who >= 0)
        {
            json.writeValue("who", who);
        }

        if (hasQuality())
        {
            json.writeValue("q", getQuality());
        }
    }

    public int getWho()
    {
        return who;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        who = jsonData.getInt("who", -1);
        amount = jsonData.getInt("amount");
        if (jsonData.has("item"))
        {
            if (item == null)
            {
                item = Map.newConsumableItem(json, jsonData.get("item"));
            }
            else
            {
                item.read(json, jsonData.get("item"));
            }
        }

        quality = jsonData.has("q") ? jsonData.getInt("q") : -1;
    }

    public void init()
    {
        item.init();
    }

    public void release()
    {
        item.release();
    }
}
