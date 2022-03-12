package com.desertkun.brainout.common.msg.client.cards;

public class GiveCardToPlayerFromTable extends CardMessage
{
    public int player;
    public int card;
    public int gave;
    public String flipped;
    public boolean animate;

    public GiveCardToPlayerFromTable() {}

    public GiveCardToPlayerFromTable(String d, int o, int player, int gave, int card, String flipped, boolean animate)
    {
        super(d, o);

        this.player = player;
        this.gave = gave;
        this.card = card;
        this.flipped = flipped;
        this.animate = animate;
    }
}
