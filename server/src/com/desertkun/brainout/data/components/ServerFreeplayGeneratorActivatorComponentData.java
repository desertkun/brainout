package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.components.ServerFreeplayGeneratorActivatorComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FreeplayGeneratorData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayItemActivatedEvent;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;

public class ServerFreeplayGeneratorActivatorComponentData extends
    Component<ServerFreeplayGeneratorActivatorComponent>
{

    public ServerFreeplayGeneratorActivatorComponentData(
        ComponentObject componentObject,
        ServerFreeplayGeneratorActivatorComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeActivateData:
            {
                ActivateActiveEvent ev = ((ActivateActiveEvent) event);
                activate(ev.client, ev.playerData);
                return true;
            }
        }

        return false;
    }

    private ActiveData findActiveData(String tag)
    {
        for (Map map : Map.All())
        {
            ActiveData found = map.getActiveNameIndex().get(tag);

            if (found != null)
                return found;
        }

        return null;
    }

    private void activate(Client client, PlayerData playerData)
    {
        Map map = getMap();

        if (map == null)
            return;

        ActiveData activeData = findActiveData(getContentComponent().getGenerator());

        if (!(activeData instanceof FreeplayGeneratorData))
            return;

        FreeplayGeneratorData generator = ((FreeplayGeneratorData) activeData);

        if (!generator.isWorking())
            return;

        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        ModePayload payload = playerClient.getModePayload();

        if (payload instanceof FreePayload)
        {
            ((FreePayload) payload).questEvent(
                FreePlayItemActivatedEvent.obtain(playerClient,
                    getContentComponent().getEvent(), 1));
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
