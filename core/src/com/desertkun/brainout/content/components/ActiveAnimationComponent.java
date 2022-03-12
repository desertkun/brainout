package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.components.ActiveAnimationComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ActiveAnimationComponent")
public class ActiveAnimationComponent extends AnimationComponent
{
    @Override
    public ActiveAnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new ActiveAnimationComponentData(componentObject, this);
    }
}
