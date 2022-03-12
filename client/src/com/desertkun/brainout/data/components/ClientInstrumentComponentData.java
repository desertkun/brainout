package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.ClientInstrumentComponent;
import com.desertkun.brainout.content.components.ReplaceInstrumentAnimationComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.CustomInstrumentEffectEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.events.LaunchAttachedEffectEvent;
import com.desertkun.brainout.events.LaunchEffectEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientInstrumentComponent")
@ReflectAlias("data.components.ClientInstrumentComponentData")
public class ClientInstrumentComponentData <T extends ClientInstrumentComponent>
        extends Component<T>
{
    private final InstrumentData instrumentData;

    public ClientInstrumentComponentData(InstrumentData instrumentData,
                                         T instrumentComponent)
    {
        super(instrumentData, instrumentComponent);

        this.instrumentData = instrumentData;
    }


    @Override
    public void init()
    {
        super.init();

        if (instrumentData.getInfo().skin != null)
        {
            ReplaceInstrumentAnimationComponent ria =
                instrumentData.getInfo().skin.getComponent(ReplaceInstrumentAnimationComponent.class);

            if (ria != null)
            {
                InstrumentAnimationComponentData ia =
                    instrumentData.getComponentWithSubclass(InstrumentAnimationComponentData.class);

                if (ia != null)
                {
                    ia.getStates().update(ria.getStates());
                }
            }
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case customInstrumentEffect:
            {
                CustomInstrumentEffectEvent e = ((CustomInstrumentEffectEvent) event);
                launchCustomEffect(e.effect);

                break;
            }
        }

        return false;
    }

    public void launchCustomEffect(String effect)
    {
        InstrumentData instrumentData = ((InstrumentData) getComponentObject());

        if (instrumentData == null)
            return;

        if (instrumentData.getOwner() == null)
            return;

        ActiveData owner = instrumentData.getOwner();

        if (owner instanceof PlayerData)
        {
            PlayerData playerData = ((PlayerData) owner);

            BrainOut.EventMgr.sendDelayedEvent(getComponentObject(),
                LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.custom,
                    playerData.getLaunchData(), effect));
        }

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
