package com.desertkun.brainout.data.block;

import com.desertkun.brainout.content.block.ArmoredConcrete;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.containers.ChunkData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.block.ArmoredConcreteBD")
public class ArmoredConcreteBD extends ConcreteBD
{
    private final ArmoredConcrete creator;

    public ArmoredConcreteBD(ArmoredConcrete creator)
    {
        super(creator);

        this.creator = creator;
    }

    @Override
    public float limitPower(float power)
    {
        if (power < creator.getMinPower())
        {
            return 0;
        }

        return super.limitPower(power);
    }
}
