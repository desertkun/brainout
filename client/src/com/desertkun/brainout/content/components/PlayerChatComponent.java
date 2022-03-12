package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.PlayerChatComponentData;
import com.desertkun.brainout.data.components.PlayerStatsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.PlayerChatComponent")
public class PlayerChatComponent extends ActiveStatsComponent
{
    @Override
    public PlayerChatComponentData getComponent(ComponentObject componentObject)
    {
        return new PlayerChatComponentData((ActiveData) componentObject, this);
    }
}
