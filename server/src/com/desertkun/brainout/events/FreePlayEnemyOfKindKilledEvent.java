package com.desertkun.brainout.events;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Player;

public class FreePlayEnemyOfKindKilledEvent extends Event
{
    public PlayerClient player;
    public Player kind;

    public FreePlayEnemyOfKindKilledEvent()
    {
    }

    @Override
    public ID getID()
    {
        return ID.freePlayEnemyOfKindKilled;
    }

    private Event init(PlayerClient player, Player kind)
    {
        this.player = player;
        this.kind = kind;

        return this;
    }

    public static Event obtain(PlayerClient player, Player kind)
    {
        FreePlayEnemyOfKindKilledEvent e = obtain(FreePlayEnemyOfKindKilledEvent.class);
        if (e == null) return null;
        return e.init(player, kind);
    }

    @Override
    public void reset()
    {
        this.player = null;
        this.kind = null;
    }
}
