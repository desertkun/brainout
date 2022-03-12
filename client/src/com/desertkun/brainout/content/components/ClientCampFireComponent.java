package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.LightEntity;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ClientCampFireComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientCampFireComponent")
public class ClientCampFireComponent extends ContentComponent
{
    private LightEntity lightEntity;

    public ClientCampFireComponent()
    {
        lightEntity = new LightEntity(false);
    }

    @Override
    public ClientCampFireComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientCampFireComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    public LightEntity getLightEntity()
    {
        return lightEntity;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        lightEntity.read(json, jsonData);
    }
}
