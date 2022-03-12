package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.PassInstrumentInfoComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("PassInstrumentInfoComponent")
@ReflectAlias("data.components.PassInstrumentInfoComponentData")
public class PassInstrumentInfoComponentData extends Component<PassInstrumentInfoComponent>
{
    private InstrumentInfo instrumentInfo;

    public PassInstrumentInfoComponentData(ComponentObject componentObject, PassInstrumentInfoComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    public void setInstrumentInfo(InstrumentInfo instrumentInfo)
    {
        this.instrumentInfo = instrumentInfo;
    }

    public InstrumentInfo getInstrumentInfo()
    {
        return instrumentInfo;
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

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
