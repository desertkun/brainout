package com.desertkun.brainout.events;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.data.active.PlayerData;

public class ActivateActiveEvent extends Event
{
    public Client client;
    public PlayerData playerData;
    public String payload;

    @Override
    public ID getID()
    {
        return ID.activeActivateData;
    }

    private Event init(Client client, PlayerData playerData, String payload)
    {
        this.client = client;
        this.playerData = playerData;
        this.payload = payload;

        return this;
    }

    public static Event obtain(Client client, PlayerData playerData, String payload)
    {
        ActivateActiveEvent e = obtain(ActivateActiveEvent.class);
        if (e == null) return null;
        return e.init(client, playerData, payload);
    }

    @Override
    public void reset()
    {
        this.playerData = null;
        this.client = null;
    }
}
