package com.desertkun.brainout.events;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.active.ActiveData;

public class FreePlayItemActivatedEvent extends Event
{
    public PlayerClient player;
    public String event;
    public int amount;

    public FreePlayItemActivatedEvent()
    {
    }

    @Override
    public ID getID()
    {
        return ID.freePlayItemActivated;
    }

    private Event init(PlayerClient player, String event, int amount)
    {
        this.player = player;
        this.event = event;
        this.amount = amount;

        return this;
    }

    public static Event obtain(PlayerClient player, String event, int amount)
    {
        FreePlayItemActivatedEvent e = obtain(FreePlayItemActivatedEvent.class);
        if (e == null) return null;
        return e.init(player, event, amount);
    }

    @Override
    public void reset()
    {
        this.player = null;
        this.event = null;
        this.amount = 0;
    }
}
