package com.desertkun.brainout.common.msg.client.cards;

public class MoveCardOnTable extends CardMessage
{
    public int card;
    public int x;
    public int y;
    public boolean animation;

    public MoveCardOnTable() {}
    public MoveCardOnTable(String d, int o, int card, int x, int y, boolean animation)
    {
        super(d, o);

        this.card = card;
        this.x = x;
        this.y = y;
        this.animation = animation;
    }
}
