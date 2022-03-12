package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.components.InstrumentHealthComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentHealthComponent")
public class InstrumentHealthComponent extends HealthComponent
{
    @Override
    public InstrumentHealthComponentData getComponent(ComponentObject componentObject)
    {
        return new InstrumentHealthComponentData((InstrumentData)componentObject, this);
    }
}
