package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.active.RealEstateItem")
public class RealEstateItem extends Active
{
    private String kind;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        kind = jsonData.getString("kind");
    }

    public String getKind()
    {
        return kind;
    }

    public boolean isStacks()
    {
        return false;
    }

    public boolean isThrowable()
    {
        return false;
    }
}
