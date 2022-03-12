package com.desertkun.brainout.data.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.active.BotSpawner;
import com.desertkun.brainout.inspection.InspectableProperty;
import com.desertkun.brainout.inspection.PropertyKind;
import com.desertkun.brainout.inspection.PropertyValue;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.BotSpawnerData")
public class BotSpawnerData extends PointData
{
    @InspectableProperty(name="Tag", kind=PropertyKind.string, value=PropertyValue.vString)
    public String tag;

    @InspectableProperty(name="Group Id", kind=PropertyKind.string, value=PropertyValue.vString)
    public String groupId;

    @InspectableProperty(name="Distance", kind=PropertyKind.string, value=PropertyValue.vFloat)
    public float distance;

    @InspectableProperty(name="Target Amount", kind=PropertyKind.string, value=PropertyValue.vInt)
    public int targetAmount;

    public BotSpawnerData(BotSpawner spawner, String dimension)
    {
        super(spawner, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        tag = jsonData.getString("tg", "");
        groupId = jsonData.getString("groupId", "");
        distance = jsonData.getFloat("distance", 0);
        targetAmount = jsonData.getInt("ta", 1);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("tg", tag);
        json.writeValue("groupId", groupId);
        json.writeValue("distance", distance);
        json.writeValue("ta", targetAmount);
    }
}
