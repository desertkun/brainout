package com.desertkun.brainout.events;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.consumable.ConsumableContent;

public class FreePlayPartnerRevivedEvent extends Event
{
    public PlayerClient player;
    public PlayerClient revived;

    public FreePlayPartnerRevivedEvent()
    {
    }

    @Override
    public ID getID()
    {
        return ID.freePlayPartnerRevived;
    }

    private FreePlayPartnerRevivedEvent init(PlayerClient player, PlayerClient revived)
    {
        this.player = player;
        this.revived = revived;

        return this;
    }

    public static FreePlayPartnerRevivedEvent obtain(PlayerClient player, PlayerClient revived)
    {
        FreePlayPartnerRevivedEvent e = obtain(FreePlayPartnerRevivedEvent.class);
        if (e == null) return null;
        return e.init(player, revived);
    }

    @Override
    public void reset()
    {
        this.player = null;
        this.revived = null;
    }
}
