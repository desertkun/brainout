package com.desertkun.brainout.events;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;

public class KillEvent extends Event
{
    public Client killer;
    public Client victim;
    public Instrument instrument;

    @Override
    public ID getID()
    {
        return ID.kill;
    }

    private Event init(Client killer, Client victim, Instrument instrument)
    {
        this.killer = killer;
        this.victim = victim;
        this.instrument = instrument;

        return this;
    }

    public static Event obtain(Client killer, Client victim, Instrument instrument)
    {
        KillEvent e = obtain(KillEvent.class);
        if (e == null) return null;
        return e.init(killer, victim, instrument);
    }

    @Override
    public void reset()
    {
        this.killer = null;
        this.victim = null;
        this.instrument = null;
    }
}
