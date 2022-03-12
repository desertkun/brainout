package com.desertkun.brainout.common.msg.client.cards;

public class DiscardCard extends CardMessage
{
    public int card;

    public DiscardCard() {}
    public DiscardCard(String d, int o, int card)
    {
        super(d, o);

        this.card = card;
    }
}
