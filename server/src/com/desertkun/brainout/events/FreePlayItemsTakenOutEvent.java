package com.desertkun.brainout.events;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.data.consumable.ConsumableContainer;

public class FreePlayItemsTakenOutEvent extends Event
{
    public PlayerClient player;
    public ObjectMap<ConsumableContent, Integer> used;
    public ObjectMap<ConsumableContent, Integer> items;

    public FreePlayItemsTakenOutEvent()
    {
        used = new ObjectMap<>();
        items = new ObjectMap<>();
    }

    @Override
    public ID getID()
    {
        return ID.freePlayItemTakenOut;
    }

    private FreePlayItemsTakenOutEvent init(PlayerClient player, ObjectMap<ConsumableContent, Integer> items)
    {
        this.player = player;
        this.items.putAll(items);

        return this;
    }

    public static FreePlayItemsTakenOutEvent obtain(PlayerClient player, ObjectMap<ConsumableContent, Integer> items)
    {
        FreePlayItemsTakenOutEvent e = obtain(FreePlayItemsTakenOutEvent.class);
        if (e == null) return null;
        return e.init(player, items);
    }

    @Override
    public void reset()
    {
        this.player = null;
        this.items.clear();
        this.used.clear();
    }
}
