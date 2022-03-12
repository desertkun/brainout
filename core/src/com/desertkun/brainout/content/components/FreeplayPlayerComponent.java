package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.FreeplayPlayerComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.FreeplayPlayerComponent")
public class FreeplayPlayerComponent extends ContentComponent
{
    private float thirstMax;
    private float temperatureMax;
    private float thirstTime;
    private float hungerMax;
    private float hungerDistance;
    private float radioMax;

    @Override
    public FreeplayPlayerComponentData getComponent(ComponentObject componentObject)
    {
        return new FreeplayPlayerComponentData((PlayerData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        thirstMax = jsonData.getFloat("thirst-max");
        temperatureMax = jsonData.getFloat("temperature-max");
        thirstTime = jsonData.getFloat("thirst-time");
        hungerMax = jsonData.getFloat("hunger-max");
        hungerDistance = jsonData.getFloat("hunger-distance");
        radioMax = jsonData.getFloat("radio-max");
    }

    public float getThirstMax()
    {
        return thirstMax;
    }

    public float getThirstTime()
    {
        return thirstTime;
    }

    public float getHungerMax()
    {
        return hungerMax;
    }

    public float getTemperatureMax()
    {
        return temperatureMax;
    }

    public float getHungerDistance()
    {
        return hungerDistance;
    }

    public float getRadioMax()
    {
        return radioMax;
    }
}
