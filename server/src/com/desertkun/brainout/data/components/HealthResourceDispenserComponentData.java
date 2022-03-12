package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.common.enums.EntityReceived;
import com.desertkun.brainout.common.msg.server.ActiveReceivedConsumableMsg;
import com.desertkun.brainout.content.components.HealthResourceDispenserComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("HealthResourceDispenserComponent")
@ReflectAlias("data.components.HealthResourceDispenserComponentData")
public class HealthResourceDispenserComponentData
    extends ResourceDispenserComponentData<HealthResourceDispenserComponent>
{
    public HealthResourceDispenserComponentData(ComponentObject componentObject,
        HealthResourceDispenserComponent resource)
    {
        super(componentObject, resource);
    }

    @Override
    public String getResourceName()
    {
        return "health";
    }

    @Override
    protected void deliverResource(ActiveData activeData)
    {
        HealthComponentData hcd = activeData.getComponent(HealthComponentData.class);

        if (hcd != null && hcd.getHealth() < hcd.getInitHealth())
        {
            float delivered = hcd.addHealth(getContentComponent().getAmount());

            hcd.updated(activeData);

            BrainOutServer.Controller.getClients().sendTCP(new ActiveReceivedConsumableMsg(activeData,
                    EntityReceived.health, (int)delivered));

            resourceDelivered(activeData, delivered);
        }
    }
}
