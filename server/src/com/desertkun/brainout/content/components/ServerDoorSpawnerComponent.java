package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ServerDoorSpawnerComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerDoorSpawnerComponent")
public class ServerDoorSpawnerComponent extends ContentComponent
{
    private Active instance;
    private String instanceName;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ServerDoorSpawnerComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        instanceName = jsonData.getString("instance");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        instance = BrainOutServer.ContentMgr.get(instanceName, Active.class);
    }

    public Active getInstance()
    {
        return instance;
    }
}
