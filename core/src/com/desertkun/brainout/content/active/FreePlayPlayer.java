package com.desertkun.brainout.content.active;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FreePlayPlayerData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.FreePlayPlayer")
public class FreePlayPlayer extends Player
{
    @Override
    public ActiveData getData(String dimension)
    {
        return new FreePlayPlayerData(this, dimension);
    }
}
