package com.desertkun.brainout.content.active;

import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.BotSpawnerData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.BotSpawner")
public class BotSpawner extends Active
{
    @Override
    public ActiveData getData(String dimension)
    {
        return new BotSpawnerData(this, dimension);
    }
}
