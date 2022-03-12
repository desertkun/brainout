package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.PlayerStatsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.PlayerStatsComponent")
public class PlayerStatsComponent extends ActiveStatsComponent
{
    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new PlayerStatsComponentData((PlayerData)componentObject, this);
    }
}
