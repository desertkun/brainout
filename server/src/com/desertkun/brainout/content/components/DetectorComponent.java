package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.DetectorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.DetectorComponent")
public class DetectorComponent extends ContentComponent
{
    private float period;
    private float x;
    private float y;
    private float width;
    private float height;
    private String detectClass;

    @Override
    public DetectorComponentData getComponent(ComponentObject componentObject)
    {
        return new DetectorComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.period = jsonData.getFloat("period");
        this.x = jsonData.getFloat("x");
        this.y = jsonData.getFloat("y");
        this.width = jsonData.getFloat("width");
        this.height = jsonData.getFloat("height");
        this.detectClass = jsonData.getString("detectClass");
    }


    public float getPeriod()
    {
        return period;
    }

    public float getHeight()
    {
        return height;
    }

    public float getWidth()
    {
        return width;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public String getDetectClass()
    {
        return detectClass;
    }
}
