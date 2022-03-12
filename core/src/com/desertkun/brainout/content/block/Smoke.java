package com.desertkun.brainout.content.block;

import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.SmokeBD;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.block.Smoke")
public class Smoke extends NonContact
{
    @Override
    public BlockData getBlock()
    {
        return new SmokeBD(this);
    }
}
