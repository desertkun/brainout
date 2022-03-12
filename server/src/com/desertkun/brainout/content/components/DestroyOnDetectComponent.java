package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.DestroyOnDetectComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.DestroyOnDetectComponent")
public class DestroyOnDetectComponent extends ContentComponent
{
    private Array<String> detectClasses;

    public DestroyOnDetectComponent()
    {
        detectClasses = new Array<>();
    }

    @Override
    public DestroyOnDetectComponentData getComponent(ComponentObject componentObject)
    {
        return new DestroyOnDetectComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("detectClasses"))
        {
            for (JsonValue entry : jsonData.get("detectClasses"))
            {
                detectClasses.add(entry.asString());
            }
        }
        else
        if (jsonData.has("detectClass"))
        {
            detectClasses.add(jsonData.getString("detectClass"));
        }
    }

    public Array<String> getDetectClasses()
    {
        return detectClasses;
    }
}
