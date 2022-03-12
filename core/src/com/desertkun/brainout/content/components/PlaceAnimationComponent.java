package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.components.PlaceAnimationComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.PlaceBlockData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.PlaceAnimationComponent")
public class PlaceAnimationComponent extends InstrumentAnimationComponent
{
    @Override
    public PlaceAnimationComponentData getComponent(ComponentObject componentObject)
    {
        return new PlaceAnimationComponentData((PlaceBlockData)componentObject, this);
    }
}
