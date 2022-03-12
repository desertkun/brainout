package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.components.ServerPickupCallbackComponent;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.EarnEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerPickupCallbackComponent")
@ReflectAlias("data.components.ServerPickupCallbackComponentData")
public class ServerPickupCallbackComponentData extends
        Component<ServerPickupCallbackComponent>
{
    private final ItemData itemData;
    private EarnedCallback callback;

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

    public interface EarnedCallback
    {
        void earned(Client client);
    }

    public ServerPickupCallbackComponentData(ItemData itemData,
                                             ServerPickupCallbackComponent itemComponent)
    {
        super(itemData, itemComponent);

        this.itemData = itemData;
        this.callback = null;
    }

    public void setCallback(EarnedCallback callback)
    {
        this.callback = callback;
    }

    protected void contentEarned(Client client)
    {
        if (callback != null)
        {
            callback.earned(client);
            callback = null;
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case earn:
            {
                EarnEvent earnEvent = ((EarnEvent) event);
                Client client = BrainOutServer.Controller.getClients().get(earnEvent.playerData.getOwnerId());

                return earn(client);
            }
        }

        return false;
    }

    protected boolean earn(Client client)
    {
        contentEarned(client);
        return getContentComponent().isBlock();
    }
}
