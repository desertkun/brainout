package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ServerPlayerColliderComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerPlayerColliderComponent")
public class ServerPlayerColliderComponent extends PlayerActiveColliderComponent
{
    @Override
    public ServerPlayerColliderComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerPlayerColliderComponentData((PlayerData)componentObject, this);
    }
}
