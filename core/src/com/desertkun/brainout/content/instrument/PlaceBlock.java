package com.desertkun.brainout.content.instrument;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.instrument.PlaceBlockData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.instrument.PlaceBlock")
public class PlaceBlock extends Instrument
{
    private float maxDistance;
    private float placeTime;
    private float damage;

    @Override
    public PlaceBlockData getData(String dimension)
    {
        return new PlaceBlockData(this, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        maxDistance = jsonData.getFloat("maxDistance");
        placeTime = jsonData.getFloat("placeTime");
        damage = jsonData.getFloat("damage");
    }

    public float getMaxDistance()
    {
        return maxDistance;
    }

    public float getPlaceTime()
    {
        return placeTime;
    }

    public float getDamage()
    {
        return damage;
    }
}
