package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.PlayerData;

public class MyPlayerSetEvent extends Event
{
    public PlayerData playerData;

    @Override
    public ID getID()
    {
        return ID.setMyPlayer;
    }

    private Event init(PlayerData playerData)
    {
        this.playerData = playerData;

        return this;
    }

    public static Event obtain(PlayerData playerData)
    {
        MyPlayerSetEvent e = obtain(MyPlayerSetEvent.class);
        if (e == null) return null;
        return e.init(playerData);
    }

    @Override
    public void reset()
    {
        this.playerData = null;
    }
}
