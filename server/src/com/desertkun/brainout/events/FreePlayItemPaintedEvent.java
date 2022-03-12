package com.desertkun.brainout.events;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.instrument.InstrumentData;

public class FreePlayItemPaintedEvent extends Event
{
    public PlayerClient player;
    public InstrumentData instrumentData;

    public FreePlayItemPaintedEvent()
    {
    }

    @Override
    public ID getID()
    {
        return ID.freePlayItemPainted;
    }

    private Event init(PlayerClient player, InstrumentData instrumentData)
    {
        this.player = player;
        this.instrumentData = instrumentData;

        return this;
    }

    public static Event obtain(PlayerClient player, InstrumentData instrumentData)
    {
        FreePlayItemPaintedEvent e = obtain(FreePlayItemPaintedEvent.class);
        if (e == null) return null;
        return e.init(player, instrumentData);
    }

    @Override
    public void reset()
    {
        this.player = null;
        this.instrumentData = null;
    }
}
