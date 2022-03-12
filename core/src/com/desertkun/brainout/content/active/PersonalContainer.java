package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PointData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.PersonalContainer")
public class PersonalContainer extends Active
{
    private String market;

    @Override
    public ActiveData getData(String dimension)
    {
        return new PointData(this, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        market = jsonData.getString("market");
    }

    public String getMarket()
    {
        return market;
    }
}
