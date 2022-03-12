package com.desertkun.brainout.events;

import com.desertkun.brainout.common.msg.server.PromoCodeResultMsg;

public class PartyStartResultEvent extends Event
{
    public boolean success;

    @Override
    public ID getID()
    {
        return ID.partyStartResult;
    }

    private Event init(boolean success)
    {
        this.success = success;

        return this;
    }

    public static Event obtain(boolean success)
    {
        PartyStartResultEvent e = obtain(PartyStartResultEvent.class);
        if (e == null) return null;
        return e.init(success);
    }

    @Override
    public void reset()
    {
        this.success = false;
    }
}
