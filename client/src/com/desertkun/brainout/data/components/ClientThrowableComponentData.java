package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.ClientThrowableComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientThrowableComponent")
@ReflectAlias("data.components.ClientThrowableComponentData")
public class ClientThrowableComponentData extends Component<ClientThrowableComponent>
{
    public ClientThrowableComponentData(InstrumentData instrumentData,
                                        ClientThrowableComponent instrumentComponent)
    {
        super(instrumentData, instrumentComponent);
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
