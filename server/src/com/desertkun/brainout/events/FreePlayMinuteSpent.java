package com.desertkun.brainout.events;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.consumable.ConsumableContent;

public class FreePlayMinuteSpent extends Event
{
    public PlayerClient player;

    public FreePlayMinuteSpent()
    {
    }

    @Override
    public ID getID()
    {
        return ID.freePlayMinuteSpent;
    }

    private FreePlayMinuteSpent init(PlayerClient player)
    {
        this.player = player;

        return this;
    }

    public static FreePlayMinuteSpent obtain(PlayerClient player)
    {
        FreePlayMinuteSpent e = obtain(FreePlayMinuteSpent.class);
        if (e == null) return null;
        return e.init(player);
    }

    @Override
    public void reset()
    {
        this.player = null;
    }
}
