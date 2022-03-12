package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.instrument.InstrumentData;

public class HookInstrumentEvent extends Event
{
    public InstrumentData selected;
    public PlayerData playerData;

    @Override
    public ID getID()
    {
        return ID.hookInstrument;
    }

    private Event init(InstrumentData selected, PlayerData playerData)
    {
        this.selected = selected;
        this.playerData = playerData;

        return this;
    }

    public static Event obtain(InstrumentData selected, PlayerData playerData)
    {
        HookInstrumentEvent e = obtain(HookInstrumentEvent.class);
        if (e == null) return null;
        return e.init(selected, playerData);
    }

    @Override
    public void reset()
    {
        this.selected = null;
        this.playerData = null;
    }
}
