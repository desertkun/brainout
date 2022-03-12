package com.desertkun.brainout.common.msg.client.cards;

public class FlipCard extends CardMessage
{
    public int card;
    public String flipTo;

    public FlipCard() {}
    public FlipCard(String d, int o, int card)
    {
        super(d, o);

        this.card = card;
        this.flipTo = null;
    }

    public FlipCard(String d, int o, int card, String flipTo)
    {
        super(d, o);

        this.card = card;
        this.flipTo = flipTo;
    }

}
