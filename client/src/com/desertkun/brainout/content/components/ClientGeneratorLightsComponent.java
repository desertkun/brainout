package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ClientGeneratorLightsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientGeneratorLightsComponent")
public class ClientGeneratorLightsComponent extends ContentComponent
{
    private Array<String> ids;
    private String on, off;

    public ClientGeneratorLightsComponent()
    {
        ids = new Array<>();
    }

    @Override
    public ClientGeneratorLightsComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientGeneratorLightsComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("ids"))
        {
            for (JsonValue value : jsonData.get("ids"))
            {
                ids.add(value.asString());
            }
        }

        on = jsonData.getString("on");
        off = jsonData.getString("off");
    }

    public Array<String> getIds()
    {
        return ids;
    }

    public String getOff()
    {
        return off;
    }

    public String getOn()
    {
        return on;
    }
}
