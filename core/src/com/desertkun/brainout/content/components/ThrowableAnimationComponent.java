package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.components.ThrowableAnimationComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ThrowableAnimationComponent")
public class ThrowableAnimationComponent extends InstrumentAnimationComponent
{
    @Override
    public ThrowableAnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new ThrowableAnimationComponentData((InstrumentData)componentObject, this);
    }
}
