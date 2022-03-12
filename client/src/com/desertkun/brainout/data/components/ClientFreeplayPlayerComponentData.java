package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.ClientFreeplayPlayerComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.events.ComponentUpdatedEvent;
import com.desertkun.brainout.events.DamagedEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ClientFreeplayPlayerComponent")
@ReflectAlias("data.components.ClientFreeplayPlayerComponentData")
public class ClientFreeplayPlayerComponentData extends ClientActiveActivatorComponentData<ClientFreeplayPlayerComponent>
{
    public ClientFreeplayPlayerComponentData(
        ActiveData activeData,
        ClientFreeplayPlayerComponent activatorComponent)
    {
        super(activeData, activatorComponent);
    }

    @Override
    public boolean test(PlayerData playerData)
    {
        if (!(getActiveData() instanceof PlayerData))
            return false;

        if (testWounded(playerData))
            return true;

        if (testSwamp(playerData))
            return true;

        return false;
    }

    private boolean testSwamp(PlayerData playerData)
    {
        PlayerData other = ((PlayerData) getActiveData());

        FreeplayPlayerComponentData otherFp = playerData.getComponent(FreeplayPlayerComponentData.class);
        FreeplayPlayerComponentData fp = other.getComponent(FreeplayPlayerComponentData.class);
        if (fp == null || otherFp == null)
            return false;

        if (!fp.isSwamp())
            return false;

        if (otherFp.isSwamp())
            return false;

        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case componentUpdated:
            {
                ComponentUpdatedEvent ev = ((ComponentUpdatedEvent) event);

                if (ev.component instanceof HealthComponentData)
                {
                    HealthComponentData hp = ((HealthComponentData) ev.component);

                    int ownerId = getActiveData().getOwnerId();
                    RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(ownerId);

                    if (remoteClient != null)
                    {
                        remoteClient.setInfoFloat("hp", hp.getHealth());
                        BrainOutClient.EventMgr.sendDelayedEvent(
                            SimpleEvent.obtain(SimpleEvent.Action.playersStatsUpdated));
                    }
                }

                break;
            }
        }

        return super.onEvent(event);
    }

    private boolean testWounded(PlayerData playerData)
    {
        PlayerData other = ((PlayerData) getActiveData());

        if (!other.isWounded())
            return false;

        if (playerData.isWounded())
            return false;

        RemoteClient me = BrainOutClient.ClientController.getMyRemoteClient();

        if (me == null)
            return false;

        String myParty = me.getPartyId();

        int ownerId = other.getOwnerId();

        RemoteClient otherClient = BrainOutClient.ClientController.getRemoteClients().get(ownerId);

        if (otherClient == null)
            return false;

        return otherClient.isFriend(me) || (myParty != null && myParty.equals(otherClient.getPartyId()));
    }


}
