package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.LightEntity;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientLightComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientLightComponent")
public class ClientLightComponent extends ContentComponent
{
    private LightEntity lightEntity;

    public ClientLightComponent()
    {
        lightEntity = new LightEntity(true);
    }

    @Override
    public ClientLightComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientLightComponentData((ActiveData)componentObject, this);
    }

    public LightEntity getLightEntity()
    {
        return lightEntity;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        lightEntity.read(json, jsonData);
    }
}
