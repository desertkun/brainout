package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.components.BlockAnimationComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BlockAnimationComponent")
public class BlockAnimationComponent extends AnimationComponent
{
    @Override
    public BlockAnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new BlockAnimationComponentData(componentObject, this);
    }
}
