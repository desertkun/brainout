package com.desertkun.brainout.common.msg.client.cards;

public class PlaceCardOnTableFromHand extends CardMessage
{
    public int card;
    public String f;
    public int player;
    public int x;
    public int y;

    public PlaceCardOnTableFromHand() {}
    public PlaceCardOnTableFromHand(String d, int o, int card, String f, int player, int x, int y)
    {
        super(d, o);

        this.card = card;
        this.f = f;
        this.player = player;
        this.x = x;
        this.y = y;
    }
}
