package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.FlagChunkComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.FlagChunkComponent")
public class FlagChunkComponent extends ContentComponent
{
    private ChunkData.ChunkFlag flag;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new FlagChunkComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        flag = ChunkData.ChunkFlag.valueOf(jsonData.getString("flag"));
    }

    public ChunkData.ChunkFlag getFlag()
    {
        return flag;
    }
}
