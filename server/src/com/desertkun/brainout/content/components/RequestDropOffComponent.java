package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.RequestDropOffComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.RequestDropOffComponent")
public class RequestDropOffComponent extends ContentComponent
{
    private String kind;
    private float time;

    @Override
    public RequestDropOffComponentData getComponent(ComponentObject componentObject)
    {
        return new RequestDropOffComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        kind = jsonData.getString("kind");
        time = jsonData.getFloat("time");
    }

    public String getKind()
    {
        return kind;
    }

    public float getTime()
    {
        return time;
    }
}
