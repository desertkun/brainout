package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ServerExitAptDoorComponentData;
import com.desertkun.brainout.data.components.ServerExitDoorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.components.ServerFreeplayExitAptDoorComponent")
public class ServerFreeplayExitAptDoorComponent extends ContentComponent
{
    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ServerExitAptDoorComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {

    }
}
