package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.PlayerActiveColliderComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.PlayerActiveColliderComponent")
public class PlayerActiveColliderComponent extends ActiveColliderComponent
{
    @Override
    public PlayerActiveColliderComponentData getComponent(ComponentObject componentObject)
    {
        return new PlayerActiveColliderComponentData((ActiveData)componentObject, this);
    }
}
