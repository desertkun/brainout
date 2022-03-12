package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.TargetBlocksSpawnerComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.TargetBlocksSpawnerComponent")
public class TargetBlocksSpawnerComponent extends ContentComponent
{
    private Block opened;
    private Block closed;

    @Override
    public TargetBlocksSpawnerComponentData getComponent(ComponentObject componentObject)
    {
        return new TargetBlocksSpawnerComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.opened = BrainOutServer.ContentMgr.get(jsonData.getString("opened"), Block.class);
        this.closed = BrainOutServer.ContentMgr.get(jsonData.getString("closed"), Block.class);
    }

    public Block getOpened()
    {
        return opened;
    }

    public Block getClosed()
    {
        return closed;
    }
}
