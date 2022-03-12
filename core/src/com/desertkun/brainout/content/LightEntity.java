package com.desertkun.brainout.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.LightEntity")
public class LightEntity implements Json.Serializable
{
    private boolean isStatic;
    private Color color;
    private float distance;
    private int rays;
    private int soft;
    private boolean xRay;

    public LightEntity(boolean isStatic)
    {
        this.isStatic = isStatic;
        this.xRay = false;
    }

    public LightEntity(LightEntity light)
    {
        this.isStatic = light.isStatic();
        this.color = new Color(light.getColor());
        this.distance = light.getDistance();
        this.rays = light.getRays();
        this.soft = light.getSoft();
        this.xRay = false;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.color = Color.valueOf(jsonData.getString("color", "333333FF"));
        this.distance = jsonData.getFloat("distance", 20);
        this.rays = jsonData.getInt("rays", 16);
        this.xRay = jsonData.getBoolean("xRay", false);
        this.soft = jsonData.getInt("soft", 0);
        this.isStatic = jsonData.getBoolean("static", isStatic);
    }

    public Color getColor()
    {
        return color;
    }

    public float getDistance()
    {
        return distance;
    }

    public int getRays()
    {
        return rays;
    }

    public int getSoft()
    {
        return soft;
    }

    public boolean isStatic()
    {
        return isStatic;
    }

    public void setColor(Color color)
    {
        this.color.set(color);
    }

    public void setDistance(float distance)
    {
        this.distance = distance;
    }

    public void setRays(int rays)
    {
        this.rays = rays;
    }

    public void setSoft(int soft)
    {
        this.soft = soft;
    }

    public boolean isxRay()
    {
        return xRay;
    }
}
