package com.desertkun.brainout.events;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.Content;

public class FreePlayItemUsedEvent extends Event
{
    public PlayerClient player;
    public Content item;
    public int amount;

    public FreePlayItemUsedEvent()
    {
    }

    @Override
    public ID getID()
    {
        return ID.freePlayItemUsed;
    }

    private Event init(PlayerClient player, Content item, int amount)
    {
        this.player = player;
        this.item = item;
        this.amount = amount;

        return this;
    }

    public static Event obtain(PlayerClient player, Content item, int amount)
    {
        FreePlayItemUsedEvent e = obtain(FreePlayItemUsedEvent.class);
        if (e == null) return null;
        return e.init(player, item, amount);
    }

    @Override
    public void reset()
    {
        this.player = null;
        this.item = null;
        this.amount = 0;
    }
}
