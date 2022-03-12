package com.desertkun.brainout.events;

import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.active.ActiveData;

public class KillEvent extends Event
{
    public Skin skin;
    public RemoteClient killer;
    public RemoteClient victim;
    public Instrument instrument;
    public ActiveData.LastHitKind kind;

    @Override
    public ID getID()
    {
        return ID.kill;
    }

    private Event init(RemoteClient killer, RemoteClient victim,
                       Instrument instrument, Skin skin, ActiveData.LastHitKind kind)
    {
        this.killer = killer;
        this.victim = victim;
        this.instrument = instrument;
        this.kind = kind;
        this.skin = skin;

        return this;
    }

    public static Event obtain(RemoteClient killer, RemoteClient victim,
                               Instrument instrument, Skin skin, ActiveData.LastHitKind kind)
    {
        KillEvent e = obtain(KillEvent.class);
        if (e == null) return null;
        return e.init(killer, victim, instrument, skin, kind);
    }

    @Override
    public void reset()
    {
        this.killer = null;
        this.victim = null;
        this.instrument = null;
        this.kind = null;
        this.skin = null;
    }
}
