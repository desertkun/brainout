package com.desertkun.brainout.common.msg.client.cards;

public class CardsTable extends CardMessage
{
    public int deckSize;
    public CardOnTable[] cardsOnTable;
    public Participant[] participants;
    public String[] discarded;

    public static class CardOnTable
    {
        public int card;
        public int x;
        public int y;
        public String flipped;
    }

    public static class Participant
    {
        public int player;
        public int place;
        public int cardsCount;
        public String[] cards;
    }

    public CardsTable() {}
    public CardsTable(String d, int o, int deckSize, CardOnTable[] cardsOnTable, Participant[] participants, String[] discarded)
    {
        super(d, o);

        this.deckSize = deckSize;
        this.cardsOnTable = cardsOnTable;
        this.participants = participants;
        this.discarded = discarded;
    }
}
