package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.server.UpdateActiveAnimationMsg;
import com.desertkun.brainout.common.msg.server.WatchAnimationMsg;
import com.desertkun.brainout.content.components.ServerFreeplayDriveAwayActivatorComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.*;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.ActivateActiveEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayItemActivatedEvent;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;

public class ServerFreeplayDriveAwayActivatorComponentData extends
    Component<ServerFreeplayDriveAwayActivatorComponent>
{

    public ServerFreeplayDriveAwayActivatorComponentData(
        ComponentObject componentObject,
        ServerFreeplayDriveAwayActivatorComponent contentComponent)
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
            drive(playerClient, ((FreePayload) payload),
                getContentComponent().getDrive(), getContentComponent().getDriveAnimations());
        }


    }

    private int getTrunkAmount()
    {
        Map map = getMap();

        if (map == null)
            return 0;

        ActiveData d = map.getActiveNameIndex().get(getContentComponent().getTrunk());
        if (!(d instanceof ItemData))
            return 0;

        ItemData itemData = ((ItemData) d);
        return itemData.getRecords().getTotalAmount();
    }

    private void drive(PlayerClient playerClient, FreePayload payload, String drive, String[] driveAnimations)
    {
        Map map = getMap();

        if (map == null)
            return;

        ActiveData d = map.getActiveNameIndex().get(drive);
        if (d == null)
            return;

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (!(gameMode.getRealization() instanceof ServerFreeRealization))
            return;

        ServerFreeRealization free = ((ServerFreeRealization) gameMode.getRealization());
        if (free == null)
            return;

        AnimationData animationData = ((AnimationData) d);

        PlayerClient partner = null;

        if (payload.hasPartyMembers())
        {
            String partyId = playerClient.getPartyId();

            if (partyId != null && !partyId.isEmpty())
            {
                for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
                {
                    if (!(entry.value instanceof PlayerClient))
                        continue;

                    PlayerClient other = ((PlayerClient) entry.value);

                    if (other == playerClient)
                        continue;

                    if (!partyId.equals(other.getPartyId()))
                        continue;

                    partner = other;
                }
            }

            if (partner != null)
            {
                partner.sendTCP(
                    new UpdateActiveAnimationMsg(animationData,
                        driveAnimations, false));

                partner.sendTCP(
                    new WatchAnimationMsg(map.getDimension(), animationData, "subroot", "vehicle-engine-run"));
            }
        }

        PlayerClient finalPartner = partner;

        payload.questEvent(
            FreePlayItemActivatedEvent.obtain(playerClient,
                getContentComponent().getEvent(), getTrunkAmount()));

        if (finalPartner != null && finalPartner.isAlive())
        {
            ModePayload modePayload = finalPartner.getModePayload();

            if (modePayload instanceof FreePayload)
            {
                FreePayload freePayload = ((FreePayload) modePayload);

                freePayload.questEvent(
                    FreePlayItemActivatedEvent.obtain(finalPartner,
                        getContentComponent().getEvent(), getTrunkAmount()));
            }
        }

        playerClient.sendTCP(
            new UpdateActiveAnimationMsg(animationData,
                driveAnimations, false));

        playerClient.sendTCP(
            new WatchAnimationMsg(map.getDimension(), animationData, "subroot", "vehicle-engine-run"));

        BrainOutServer.PostRunnable(() ->
        {
            free.playerExit(playerClient.getPlayerData(), playerClient);

            if (finalPartner != null && finalPartner.isAlive())
            {
                free.playerExit(finalPartner.getPlayerData(), finalPartner);
            }
        });
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
