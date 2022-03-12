package com.desertkun.brainout.common.msg.client.cards;

public class GiveCardToPlayerFromDeck extends CardMessage
{
    public int player;
    public int gave;
    public String card;
    public int deckSize;
    public boolean animate;

    public GiveCardToPlayerFromDeck() {}
    public GiveCardToPlayerFromDeck(String d, int o, int player, boolean animate)
    {
        super(d, o);

        this.player = player;
        this.gave = -1;
        this.deckSize = 0;
        this.card = null;
        this.animate = animate;
    }

    public GiveCardToPlayerFromDeck(String d, int o, int player, int gave, int deckSize, boolean animate)
    {
        super(d, o);

        this.player = player;
        this.gave = gave;
        this.card = null;
        this.deckSize = deckSize;
        this.animate = animate;
    }

    public GiveCardToPlayerFromDeck(String d, int o, int player, int gave, String card, int deckSize, boolean animate)
    {
        super(d, o);

        this.gave = gave;
        this.player = player;
        this.card = card;
        this.deckSize = deckSize;
        this.animate = animate;
    }
}
