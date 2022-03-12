package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.PlayerData;

public class EarnEvent extends Event
{
    public PlayerData playerData;

    @Override
    public ID getID()
    {
        return ID.earn;
    }

    private Event init(PlayerData playerData)
    {
        this.playerData = playerData;

        return this;
    }

    public static Event obtain(PlayerData playerData)
    {
        EarnEvent e = obtain(EarnEvent.class);
        if (e == null) return null;
        return e.init(playerData);
    }

    @Override
    public void reset()
    {
        this.playerData = null;
    }
}
