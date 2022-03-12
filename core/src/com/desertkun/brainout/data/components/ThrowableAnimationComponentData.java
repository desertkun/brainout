package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.ThrowableAnimationComponent;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.*;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ThrowableAnimationComponent")
@ReflectAlias("data.components.ThrowableAnimationComponentData")
public class ThrowableAnimationComponentData extends InstrumentAnimationComponentData<ThrowableAnimationComponent>
{
    private final InstrumentData instrumentData;

    public ThrowableAnimationComponentData(InstrumentData instrumentData,
                                           ThrowableAnimationComponent instrumentComponent)
    {
        super(instrumentData, instrumentComponent);

        this.instrumentData = instrumentData;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case launchEffect:
            {
                LaunchEffectEvent effectEvent = ((LaunchEffectEvent) event);

                if (effectEvent.kind == LaunchEffectEvent.Kind.shoot)
                {
                    BrainOut.EventMgr.sendDelayedEvent(instrumentData.getOwner(),
                        InstrumentActionEvent.obtain(Instrument.Action.shoot));
                }

                break;
            }
        }

        return super.onEvent(event);
    }
}
