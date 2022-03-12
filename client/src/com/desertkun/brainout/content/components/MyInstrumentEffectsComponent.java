package com.desertkun.brainout.content.components;

import com.desertkun.brainout.data.components.MyInstrumentComponentData;
import com.desertkun.brainout.data.components.MyInstrumentEffectsComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.MyInstrumentEffectsComponent")
public class MyInstrumentEffectsComponent extends MyInstrumentComponent
{
    @Override
    public MyInstrumentComponentData getComponent(ComponentObject componentObject)
    {
        return new MyInstrumentEffectsComponentData(componentObject, this);
    }
}
