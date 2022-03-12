package com.desertkun.brainout.content.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.InstrumentLightComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentLightComponent")
public class InstrumentLightComponent extends ContentComponent
{
    private Color color;
    private float distance;
    private float cone;
    private int rays;
    private float soft;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new InstrumentLightComponentData((InstrumentData) componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.color = Color.valueOf(jsonData.getString("color", "FFFFFFFF"));
        this.distance = jsonData.getFloat("distance", 20);
        this.cone = jsonData.getFloat("cone", 10);
        this.rays = jsonData.getInt("rays", 8);
        this.soft = jsonData.getFloat("soft", 0);
    }

    public Color getColor()
    {
        return color;
    }

    public float getDistance()
    {
        return distance;
    }

    public void setDistance(float distance)
    {
        this.distance = distance;
    }

    public float getCone()
    {
        return cone;
    }

    public int getRays()
    {
        return rays;
    }

    public float getSoft()
    {
        return soft;
    }
}
