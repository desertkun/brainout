package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ServerSmokeGeneratorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerSmokeGeneratorComponent")
public class ServerSmokeGeneratorComponent extends ContentComponent
{
    private float activateTime;
    private Block block;
    private String activateEffect;

    @Override
    public ServerSmokeGeneratorComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerSmokeGeneratorComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    public float getActivateTime()
    {
        return activateTime;
    }

    public Block getBlock()
    {
        return block;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        activateEffect = jsonData.getString("activate");
        activateTime = jsonData.getFloat("activate-time");
        block = BrainOutServer.ContentMgr.get(jsonData.getString("block"), Block.class);
    }

    public String getActivateEffect()
    {
        return activateEffect;
    }
}
