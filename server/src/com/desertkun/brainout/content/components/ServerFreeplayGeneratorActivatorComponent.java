package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.ServerFreeplayGeneratorActivatorComponentData;
import com.desertkun.brainout.data.components.ServerPortalComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerFreeplayGeneratorActivatorComponent")
public class ServerFreeplayGeneratorActivatorComponent extends ContentComponent
{
    private String event;
    private String generator;

    @Override
    public ServerFreeplayGeneratorActivatorComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerFreeplayGeneratorActivatorComponentData(componentObject, this);
    }

    public ServerFreeplayGeneratorActivatorComponent()
    {
        event = "";
        generator = "";
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.event = jsonData.getString("event", "");
        this.generator = jsonData.getString("generator", "");
    }

    public String getEvent()
    {
        return event;
    }

    public String getGenerator()
    {
        return generator;
    }
}
