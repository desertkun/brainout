package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.components.SpawnAnimationComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.SpawnAnimationComponent")
public class SpawnAnimationComponent extends AnimationComponent
{
    @Override
    public SpawnAnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new SpawnAnimationComponentData(componentObject, this);
    }
}
