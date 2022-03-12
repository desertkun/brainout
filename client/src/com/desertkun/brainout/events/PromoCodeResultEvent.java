package com.desertkun.brainout.events;

import com.desertkun.brainout.common.msg.server.PromoCodeResultMsg;

public class PromoCodeResultEvent extends Event
{
    public PromoCodeResultMsg.Result result;

    @Override
    public ID getID()
    {
        return ID.promoCodeResult;
    }

    private Event init(PromoCodeResultMsg.Result result)
    {
        this.result = result;

        return this;
    }

    public static Event obtain(PromoCodeResultMsg.Result result)
    {
        PromoCodeResultEvent e = obtain(PromoCodeResultEvent.class);
        if (e == null) return null;
        return e.init(result);
    }

    @Override
    public void reset()
    {
        this.result = null;
    }
}
