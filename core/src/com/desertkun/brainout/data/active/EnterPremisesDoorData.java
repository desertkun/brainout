package com.desertkun.brainout.data.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.active.EnterPremisesDoor;
import com.desertkun.brainout.content.active.FreeplayExitDoor;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("data.active.EnterPremisesDoorData")
public class EnterPremisesDoorData extends PointData
{
    private float exitTime;

    @InspectableProperty(name = "location", kind = PropertyKind.string, value = PropertyValue.vString)
    public String location;

    public EnterPremisesDoorData(EnterPremisesDoor door, String dimension)
    {
        super(door, dimension);

        exitTime = door.getExitTime();
        location = "";

        setzIndex(1);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("exitTime", exitTime);
        json.writeValue("location", location);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        exitTime = jsonData.getFloat("exitTime", 1.0f);
        location = jsonData.getString("location", "");
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public int getZIndex()
    {
        return 15;
    }
}
