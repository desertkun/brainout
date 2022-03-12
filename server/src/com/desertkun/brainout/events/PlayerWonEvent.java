package com.desertkun.brainout.events;

import com.desertkun.brainout.client.Client;

public class PlayerWonEvent extends Event
{
    public Client client;

    @Override
    public ID getID()
    {
        return ID.playerWon;
    }

    private Event init(Client client)
    {
        this.client = client;

        return this;
    }

    public static Event obtain(Client client)
    {
        PlayerWonEvent e = obtain(PlayerWonEvent.class);
        if (e == null) return null;
        return e.init(client);
    }

    @Override
    public void reset()
    {
        this.client = null;
    }
}
