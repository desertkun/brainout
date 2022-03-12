package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ServerCampFireComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerCampFireComponent")
public class ServerCampFireComponent extends ContentComponent
{
    private String addFuel;

    @Override
    public ServerCampFireComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerCampFireComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    public String getAddFuel()
    {
        return addFuel;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        addFuel = jsonData.getString("addFuel");
    }
}
