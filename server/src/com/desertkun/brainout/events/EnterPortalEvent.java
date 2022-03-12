package com.desertkun.brainout.events;

import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.PortalData;

public class EnterPortalEvent extends Event
{
    public PlayerData playerData;
    public PortalData enter;
    public PortalData exit;

    @Override
    public ID getID()
    {
        return ID.enterPortal;
    }

    private Event init(PlayerData playerData, PortalData enter, PortalData exit)
    {
        this.playerData = playerData;
        this.enter = enter;
        this.exit = exit;

        return this;
    }

    public static Event obtain(PlayerData playerData, PortalData enter, PortalData exit)
    {
        EnterPortalEvent e = obtain(EnterPortalEvent.class);
        if (e == null) return null;
        return e.init(playerData, enter, exit);
    }

    @Override
    public void reset()
    {
        this.playerData = null;
        this.enter = null;
        this.exit = null;
    }
}
