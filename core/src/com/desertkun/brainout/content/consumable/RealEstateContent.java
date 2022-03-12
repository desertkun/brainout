package com.desertkun.brainout.content.consumable;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.consumable.RealEstateContent")
public class RealEstateContent extends Content
{
    private String map;
    private String location;
    private String variant;
    private String rooms;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        map = jsonData.getString("map");
        location = jsonData.getString("location");
        variant = jsonData.getString("variant");
        rooms = jsonData.getString("rooms");
    }

    public String getMap()
    {
        return map;
    }

    public String getLocation()
    {
        return location;
    }

    public String getRooms()
    {
        return rooms;
    }

    public String getVariant()
    {
        return variant;
    }

    public boolean isStacks()
    {
        return false;
    }

    public boolean isThrowable()
    {
        return false;
    }
}
