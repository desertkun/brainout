package com.desertkun.brainout.content.consumable.impl;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.RealEstateContent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("RealEstateConsumableItem")
@ReflectAlias("content.consumable.impl.RealEstateConsumableItem")
public class RealEstateConsumableItem extends ConsumableItem
{
    private final RealEstateContent realEstate;
    private String location;
    private String id;

    public RealEstateConsumableItem(RealEstateContent realEstate)
    {
        this.realEstate = realEstate;
    }

    public String getLocation()
    {
        return location;
    }

    public String getId()
    {
        return id;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public RealEstateContent getContent()
    {
        return realEstate;
    }

    @Override
    public boolean stacks(ConsumableItem item)
    {
        return false;
    }

    public RealEstateContent getRealEstate()
    {
        return realEstate;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        location = jsonData.getString("l");
        id = jsonData.getString("id");
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("l", location);
        json.writeValue("id", id);
    }
}
