package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;

public class ItemTakingEvent extends Event
{
    public PlayerData playerData;
    public ItemData itemData;

    @Override
    public ID getID()
    {
        return ID.itemTaking;
    }

    private Event init(PlayerData playerData, ItemData itemData)
    {
        this.playerData = playerData;
        this.itemData = itemData;

        return this;
    }

    public static Event obtain(PlayerData playerData, ItemData itemData)
    {
        ItemTakingEvent e = obtain(ItemTakingEvent.class);
        if (e == null) return null;
        return e.init(playerData, itemData);
    }

    @Override
    public void reset()
    {
        this.playerData = null;
        this.itemData = null;
    }
}
