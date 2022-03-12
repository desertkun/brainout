package com.desertkun.brainout.events;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.consumable.ConsumableContent;

public class FreePlayItemBurnedEvent extends Event
{
    public PlayerClient player;
    public ConsumableContent item;
    public int amount;

    public FreePlayItemBurnedEvent()
    {
    }

    @Override
    public ID getID()
    {
        return ID.freePlayItemBurned;
    }

    private Event init(PlayerClient player, ConsumableContent item, int amount)
    {
        this.player = player;
        this.item = item;
        this.amount = amount;

        return this;
    }

    public static Event obtain(PlayerClient player, ConsumableContent item, int amount)
    {
        FreePlayItemBurnedEvent e = obtain(FreePlayItemBurnedEvent.class);
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
