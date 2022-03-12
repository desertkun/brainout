package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.EnterPremisesDoorData;
import com.desertkun.brainout.data.components.ServerEnterDoorComponentData;
import com.desertkun.brainout.data.components.ServerEnterRealEstateComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.components.ServerFreeplayEnterRealEstateComponent")
public class ServerFreeplayEnterRealEstateComponent extends ContentComponent
{
    public ServerFreeplayEnterRealEstateComponent()
    {
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ServerEnterRealEstateComponentData((EnterPremisesDoorData)componentObject, this);
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
