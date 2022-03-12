package com.desertkun.brainout.content.instrument;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.data.instrument.BoxData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.instrument.Box")
public class Box extends PlaceBlock
{
    private Block block;

    @Override
    public BoxData getData(String dimension)
    {
        return new BoxData(this, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.block = (Block) BrainOut.ContentMgr.get(jsonData.getString("block"));
    }

    public Block getBlock()
    {
        return block;
    }
}
