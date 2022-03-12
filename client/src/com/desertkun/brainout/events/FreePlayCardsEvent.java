package com.desertkun.brainout.events;

import com.desertkun.brainout.common.msg.client.cards.CardMessage;

public class FreePlayCardsEvent extends Event
{
    public CardMessage msg;

    @Override
    public ID getID()
    {
        return ID.freePlayCards;
    }

    private Event init(CardMessage msg)
    {
        this.msg = msg;

        return this;
    }

    public static Event obtain(CardMessage msg)
    {
        FreePlayCardsEvent e = obtain(FreePlayCardsEvent.class);
        if (e == null) return null;
        return e.init(msg);
    }

    @Override
    public void reset()
    {
        this.msg = null;
    }
}
