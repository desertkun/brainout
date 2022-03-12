package com.desertkun.brainout.content.block;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.ConcreteBD;
import com.desertkun.brainout.data.block.NonContactBD;
import com.desertkun.brainout.data.containers.ChunkData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.block.NonContact")
public class NonContact extends Block
{
    private boolean fixture;
    private boolean editor;

    @Override
    public BlockData getBlock()
    {
        if (static_)
        {
            if (instance == null)
            {
                instance = new NonContactBD(this);
            }

            return instance;
        }

        return new NonContactBD(this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        if (jsonData.has("fixture"))
        {
            fixture = jsonData.getBoolean("fixture");
        }

        editor = jsonData.getBoolean("editor", true);
    }

    public boolean isFixture()
    {
        return fixture;
    }

    @Override
    public boolean isEditorSelectable()
    {
        return editor;
    }
}
