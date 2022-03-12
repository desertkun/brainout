package com.desertkun.brainout.data.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.FreeplayExitDoor;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.active.FreeplayExitDoorData")
public class FreeplayExitDoorData extends PointData implements WithTag
{
    private float exitTime;

    public FreeplayExitDoorData(FreeplayExitDoor door, String dimension)
    {
        super(door, dimension);

        exitTime = door.getExitTime();

        setzIndex(1);
    }

    @Override
    public void write(Json json)
    {
        super.write(json);

        json.writeValue("exitTime", exitTime);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        exitTime = jsonData.getFloat("exitTime", 1.0f);
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

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.EXIT_DOOR);
    }
}
