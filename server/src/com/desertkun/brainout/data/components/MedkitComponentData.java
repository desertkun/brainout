package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.enums.EntityReceived;
import com.desertkun.brainout.common.msg.server.ActiveReceivedConsumableMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.components.MedkitComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.ActivateInstrumentEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("MedkitComponent")
@ReflectAlias("data.components.MedkitComponentData")
public class MedkitComponentData extends Component<MedkitComponent>
{
    private final InstrumentData instrumentData;

    private float timer;
    private float amount;
    private boolean activated;
    private ConsumableRecord record;

    public MedkitComponentData(InstrumentData instrumentData,
       MedkitComponent medkitComponent)
    {
        super(instrumentData, medkitComponent);

        this.instrumentData = instrumentData;

        this.activated = false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case instrumentActivate:
            {
                ActivateInstrumentEvent instrumentEvent = ((ActivateInstrumentEvent) event);

                activate(instrumentEvent.record);

                break;
            }
        }

        return false;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (activated)
        {
            timer -= dt;
            if (timer < 0)
            {
                if (this.amount <= 0)
                {
                    destroy();
                }

                timer = getContentComponent().getPeriod();
                final float toDeliver = Math.min(this.amount, getContentComponent().getAmount());

                this.amount -= toDeliver;

                BrainOutServer.PostRunnable(() ->
                {
                    final ActiveData owner = instrumentData.getOwner();
                    final HealthComponentData health = owner.getComponent(HealthComponentData.class);

                    float delivered = health.addHealth(toDeliver);

                    health.updated(owner);

                    BrainOutServer.Controller.getClients().sendTCP(new ActiveReceivedConsumableMsg(owner,
                            EntityReceived.health, (int) delivered));
                });
            }
        }
    }

    private void destroy()
    {
        activated = false;

        final ActiveData owner = instrumentData.getOwner();
        final PlayerOwnerComponent poc = owner.getComponent(PlayerOwnerComponent.class);

        if (poc == null) return;
        final Client client = BrainOutServer.Controller.getClients().getByActive(owner);

        BrainOutServer.PostRunnable(() ->
        {
            poc.getConsumableContainer().getConsumable(1, record);

            if (client != null)
            {
                client.sendConsumable();
            }
        });
    }

    private void activate(final ConsumableRecord record)
    {
        final ActiveData owner = instrumentData.getOwner();
        this.record = record;

        if (owner != null)
        {
            if (!activated)
            {
                final HealthComponentData health = owner.getComponent(HealthComponentData.class);
                if (health.getHealth() == health.getInitHealth()) return;

                amount = getContentComponent().getHealth();
                activated = true;
            }
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
        return true;
    }
}
