package com.desertkun.brainout.data.block;

import com.desertkun.brainout.content.block.BackgroundBlock;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.containers.ChunkData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.block.BackgroundBD")
public class BackgroundBD extends NonContactBD
{
    public BackgroundBD(BackgroundBlock creator)
    {
        super(creator);
    }
}
