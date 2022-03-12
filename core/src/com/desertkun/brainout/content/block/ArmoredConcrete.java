package com.desertkun.brainout.content.block;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.ArmoredConcreteBD;
import com.desertkun.brainout.data.block.ConcreteBD;
import com.desertkun.brainout.data.containers.ChunkData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.block.ArmoredConcrete")
public class ArmoredConcrete extends Concrete
{
    private float minPower;

    @Override
    public ArmoredConcreteBD getBlock()
    {
        if (static_)
        {
            if (instance == null)
            {
                instance = new ArmoredConcreteBD(this);
            }

            return (ArmoredConcreteBD)instance;
        }

        return new ArmoredConcreteBD(this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        minPower = jsonData.getFloat("minPower");
    }

    public float getMinPower()
    {
        return minPower;
    }
}
