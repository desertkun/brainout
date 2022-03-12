package com.desertkun.brainout.common.msg.client.cards;

public class JoinCards extends CardMessage
{
    public int place;

    public JoinCards() {}
    public JoinCards(String d, int o, int place)
    {
        super(d, o);

        this.place = place;
    }
}
