package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.active.FreePlayPlayerData;
import com.desertkun.brainout.data.components.FreePlayerStatsComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.FreePlayerStatsComponent")
public class FreePlayerStatsComponent extends ActiveStatsComponent
{
    @Override
    public FreePlayerStatsComponentData getComponent(ComponentObject componentObject)
    {
        return new FreePlayerStatsComponentData((FreePlayPlayerData)componentObject, this);
    }
}
