package com.desertkun.brainout.events;

import com.desertkun.brainout.client.PlayerClient;

public class FreePlayWeaponUpgradedEvent extends Event
{
    public PlayerClient player;

    public FreePlayWeaponUpgradedEvent()
    {
    }

    @Override
    public ID getID()
    {
        return ID.freePlayWeaponUpgraded;
    }

    private FreePlayWeaponUpgradedEvent init(PlayerClient player)
    {
        this.player = player;

        return this;
    }

    public static FreePlayWeaponUpgradedEvent obtain(PlayerClient player)
    {
        FreePlayWeaponUpgradedEvent e = obtain(FreePlayWeaponUpgradedEvent.class);
        if (e == null) return null;
        return e.init(player);
    }

    @Override
    public void reset()
    {
        this.player = null;
    }
}
