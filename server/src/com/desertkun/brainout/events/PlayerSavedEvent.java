package com.desertkun.brainout.events;

import com.desertkun.brainout.client.Client;

public class PlayerSavedEvent extends Event
{
    public Client client;

    @Override
    public ID getID()
    {
        return ID.playerSaved;
    }

    private Event init(Client client)
    {
        this.client = client;

        return this;
    }

    public static Event obtain(Client client)
    {
        PlayerSavedEvent e = obtain(PlayerSavedEvent.class);
        if (e == null) return null;
        return e.init(client);
    }

    @Override
    public void reset()
    {
        this.client = null;
    }
}
