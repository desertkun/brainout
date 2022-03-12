package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.EnterPremisesDoorData;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.active.EnterPremisesDoor")
public class EnterPremisesDoor extends Active
{
    private float exitTime;

    @Override
    public EnterPremisesDoorData getData(String dimension)
    {
        return new EnterPremisesDoorData(this, dimension);
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
