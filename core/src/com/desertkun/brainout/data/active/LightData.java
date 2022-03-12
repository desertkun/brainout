package com.desertkun.brainout.data.active;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.LightEntity;
import com.desertkun.brainout.content.active.Light;
import com.desertkun.brainout.inspection.InspectableGetter;
import com.desertkun.brainout.inspection.InspectableSetter;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.LightData")
public class LightData extends PointData
{
    private LightEntity lightEntity;

    public LightData(Light light, String dimension)
    {
        super(light, dimension);

        this.lightEntity = new LightEntity(light.getLightEntity());
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("clr", lightEntity.getColor().toString());
        json.writeValue("dst", lightEntity.getDistance());
    }

    public Color parserColor(String color)
    {
        return Color.valueOf(color);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        lightEntity.setColor(parserColor(jsonData.getString("clr", "FFFFFFFF")));
        lightEntity.setDistance(jsonData.getFloat("dst", 20));
    }

    public Color getParsedColor()
    {
        return parserColor(getColor_());
    }

    @InspectableGetter(name="color", kind=PropertyKind.string, value=PropertyValue.vString)
    public String getColor_()
    {
        return lightEntity.getColor().toString();
    }

    @InspectableSetter(name="color")
    public void setColor_(String color)
    {
        this.lightEntity.setColor(Color.valueOf(color));
    }

    @InspectableGetter(name="distance", kind=PropertyKind.string, value=PropertyValue.vFloat)
    public float getDistance()
    {
        return lightEntity.getDistance();
    }

    @InspectableSetter(name="distance")
    public void setDistance(float distance)
    {
        this.lightEntity.setDistance(distance);
    }

    public LightEntity getLightEntity()
    {
        return lightEntity;
    }
}
