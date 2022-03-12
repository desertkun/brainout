package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.InstrumentActionsComponent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.AnimationActionEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("InstrumentActionsComponent")
@ReflectAlias("data.components.InstrumentActionsComponentData")
public class InstrumentActionsComponentData extends Component<InstrumentActionsComponent>
{
    private final InstrumentData instrumentData;

    public InstrumentActionsComponentData(InstrumentData instrumentData,
                                          InstrumentActionsComponent component)
    {
        super(instrumentData, component);

        this.instrumentData = instrumentData;
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
        switch (event.getID())
        {
            case animationAction:
            {
                AnimationActionEvent actionEvent = ((AnimationActionEvent) event);

                if (actionEvent.kind.equals("instrument-action"))
                {
                    getContentComponent().process(instrumentData, actionEvent.payload);
                }

                return true;
            }
        }

        return false;
    }
}
