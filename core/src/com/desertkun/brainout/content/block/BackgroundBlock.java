package com.desertkun.brainout.content.block;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BackgroundBD;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.ConcreteBD;
import com.desertkun.brainout.data.containers.ChunkData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.block.BackgroundBlock")
public class BackgroundBlock extends NonContact
{
    @Override
    public BackgroundBD getBlock()
    {
        if (static_)
        {
            if (instance == null)
            {
                instance = new BackgroundBD(this);
            }

            return (BackgroundBD)instance;
        }

        return new BackgroundBD(this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
    }

    @Override
    public int getDefaultLayer()
    {
        return Constants.Layers.BLOCK_LAYER_BACKGROUND;
    }

    @Override
    public boolean isEditorSelectable()
    {
        return true;
    }
}
