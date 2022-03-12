package com.desertkun.brainout.data.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.PointData")
public class PointData extends ActiveData
{
    protected float x;
    protected float y;
    protected float angle;

    @InspectableProperty(name = "flipX", kind = PropertyKind.checkbox, value = PropertyValue.vBoolean)
    public boolean flipX;

    public PointData(Active active, String dimension)
    {
        super(active, dimension);

        setzIndex(2);
    }

    @Override
    public float getX()
    {
        return x;
    }

    @Override
    public float getY()
    {
        return y;
    }

    @Override
    public float getAngle()
    {
        return angle;
    }

    public void setX(float x)
    {
        this.x = x;
    }

    public void setY(float y)
    {
        this.y = y;
    }

    @Override
    public void setAngle(float angle)
    {
        this.angle = angle;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        x = jsonData.getFloat("x");
        y = jsonData.getFloat("y");
        angle = jsonData.getFloat("an", angle);

        flipX = jsonData.getBoolean("f", false);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("x", x);
        json.writeValue("y", y);
        json.writeValue("an", angle);

        if (flipX)
        {
            json.writeValue("f", true);
        }
    }

    public boolean isFlipX()
    {
        return flipX;
    }
}
