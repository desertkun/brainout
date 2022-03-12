package com.desertkun.brainout.common.msg.client.cards;

public class TakeCardOffDeckOntoTable extends CardMessage
{
    public int card;
    public int x;
    public int y;
    public boolean animate;
    public int deckSize;

    public TakeCardOffDeckOntoTable() {}
    public TakeCardOffDeckOntoTable(String d, int o, int x, int y)
    {
        super(d, o);

        this.card = -1;
        this.x = x;
        this.y = y;
        this.animate = false;
    }

    public TakeCardOffDeckOntoTable(String d, int o, int card, int x, int y, boolean animate, int deckSize)
    {
        super(d, o);

        this.card = card;
        this.x = x;
        this.y = y;
        this.animate = animate;
        this.deckSize = deckSize;
    }
}
