package com.desertkun.brainout.events;

import com.desertkun.brainout.common.msg.server.PromoCodeResultMsg;

public class NewOrderResultEvent extends Event
{
    public boolean success;
    public String reason;

    @Override
    public ID getID()
    {
        return ID.newOrderResult;
    }

    private Event init(boolean success, String reason)
    {
        this.success = success;
        this.reason = reason;

        return this;
    }

    public static Event obtain(boolean success, String reason)
    {
        NewOrderResultEvent e = obtain(NewOrderResultEvent.class);
        if (e == null) return null;
        return e.init(success, reason);
    }

    @Override
    public void reset()
    {
        this.success = false;
        this.reason = null;
    }
}
