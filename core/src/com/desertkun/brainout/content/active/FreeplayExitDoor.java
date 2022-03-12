package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.FreeplayExitDoorData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.FreeplayExitDoor")
public class FreeplayExitDoor extends Active
{
    private float exitTime;

    @Override
    public FreeplayExitDoorData getData(String dimension)
    {
        return new FreeplayExitDoorData(this, dimension);
    }

    @Override
    public boolean isEditorSelectable()
    {
        return true;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        exitTime = jsonData.getFloat("exitTime");
    }

    public float getExitTime()
    {
        return exitTime;
    }
}
