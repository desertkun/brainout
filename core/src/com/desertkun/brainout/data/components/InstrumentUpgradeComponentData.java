package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.InstrumentUpgradeComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("InstrumentUpgradeComponent")
@ReflectAlias("data.components.InstrumentUpgradeComponentData")
public class InstrumentUpgradeComponentData extends Component<InstrumentUpgradeComponent>
{
    public InstrumentUpgradeComponentData(ComponentObject componentObject,
                                          InstrumentUpgradeComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }
}
