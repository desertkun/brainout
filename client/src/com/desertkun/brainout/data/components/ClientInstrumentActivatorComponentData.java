package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.client.ActivateInstrumentMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.ClientInstrumentActivatorComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.ActivateInstrumentEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientInstrumentActivatorComponent")
@ReflectAlias("data.components.ClientInstrumentActivatorComponentData")
public class ClientInstrumentActivatorComponentData extends Component<ClientInstrumentActivatorComponent>
{
    private final InstrumentData instrumentData;

    public ClientInstrumentActivatorComponentData(InstrumentData instrumentData,
                                                  ClientInstrumentActivatorComponent activatorComponent)
    {
        super(instrumentData, activatorComponent);

        this.instrumentData = instrumentData;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case instrumentActivate:
            {
                ActivateInstrumentEvent activate = ((ActivateInstrumentEvent) event);

                activate(activate.record);

                return true;
            }
        }

        return false;
    }

    public void activate(ConsumableRecord record)
    {
        ActiveData owner = instrumentData.getOwner();

        if (owner instanceof PlayerData)
        {
            getContentComponent().getActivateEffect().launchEffects(((PlayerData) owner).getLaunchData());
        }


        PlayerOwnerComponent poc = owner.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            BrainOutClient.ClientController.sendTCP(new ActivateInstrumentMsg(record.getId()));
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
