package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.components.ActiveAnimationComponentData;
import com.desertkun.brainout.data.components.WindAnimationComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.WindAnimationComponent")
public class WindAnimationComponent extends AnimationComponent
{
    @Override
    public WindAnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new WindAnimationComponentData(componentObject, this);
    }
}
