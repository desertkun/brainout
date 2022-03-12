package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.client.cards.*;
import com.desertkun.brainout.content.components.ServerDeckOfCardsComponent;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerDeckOfCardsComponent")
@ReflectAlias("data.components.ServerDeckOfCardsComponentData")
public class ServerDeckOfCardsComponentData extends Component<ServerDeckOfCardsComponent>
{
    private Array<String> deck;
    private Array<String> discard;
    private Array<TableCard> tableCards;
    private IntMap<Participant> participants;
    private IntSet listeners;
    private IntSet freePlaces;
    private float check;

    private static class Participant
    {
        private Array<String> hand;
        private int place;

        public Participant(int place)
        {
            this.hand = new Array<>();
            this.place = place;
        }

        public Array<String> getHand()
        {
            return hand;
        }

        public boolean give(String card)
        {
            if (hand.contains(card, false))
                return false;

            hand.add(card);
            return true;
        }

        public boolean take(String card)
        {
            if (!hand.contains(card, false))
                return false;

            hand.removeValue(card, false);
            return true;
        }
    }

    private static class TableCard
    {
        private static int NEXT_ID = 0;
        public int id;
        public boolean flipped;
        public String card;
        public int x;
        public int y;

        public TableCard(String card, int x, int y)
        {
            this.id = NEXT_ID++;
            this.card = card;
            this.x = x;
            this.y = y;
            this.flipped = false;
        }

        public void move(int x, int y)
        {
            this.x = x;
            this.y = y;
        }
    }

    public ServerDeckOfCardsComponentData(
        ComponentObject componentObject,
        ServerDeckOfCardsComponent contentComponent)
    {
        super(componentObject, contentComponent);

        deck = new Array<>(contentComponent.getDeck());
        participants = new IntMap<>();
        tableCards = new Array<>();
        discard = new Array<>();
        listeners = new IntSet();

        freePlaces = new IntSet();
        freePlaces.add(0);
        freePlaces.add(1);
    }

    private TableCard findCard(String card)
    {
        for (TableCard tableCard : tableCards)
        {
            if (tableCard.card.equals(card))
                return tableCard;
        }

        return null;
    }

    private TableCard findCard(int id)
    {
        for (TableCard tableCard : tableCards)
        {
            if (tableCard.id == id)
                return tableCard;
        }

        return null;
    }

    private boolean validateTable(int x, int y)
    {
        if (x > 600 || x < 0)
            return false;
        if (y > 600 || y < 0)
            return false;
        return true;
    }

    private boolean validateParticipant(int owner)
    {
        return participants.containsKey(owner);
    }

    public int takeCardOffDeckOntoTable(int owner, int x, int y)
    {
        if (!validateParticipant(owner))
            return -1;
        if (deck.size == 0)
            return -1;
        if (!validateTable(x, y))
            return -1;
        String card = deck.pop();
        TableCard tableCard = new TableCard(card, x, y);
        tableCards.add(tableCard);
        notifyExcept(new TakeCardOffDeckOntoTable(d(), o(), tableCard.id, x, y, true, deck.size), owner);
        notifyTo(owner, new TakeCardOffDeckOntoTable(d(), o(), tableCard.id, x, y, false, deck.size));
        return tableCard.id;
    }

    public int placeCardOnTableFromHand(int owner, String card, int x, int y)
    {
        if (!validateTable(x, y))
            return -1;
        Participant participant = participants.get(owner);
        if (participant == null)
            return -1;
        if (!participant.take(card))
            return -1;
        TableCard tableCard = new TableCard(card, x, y);
        tableCard.flipped = true;
        tableCards.add(tableCard);

        notifyAll(new PlaceCardOnTableFromHand(d(), o(), tableCard.id, card, owner, x, y));
        return tableCard.id;
    }

    public String flipCard(int owner, int card)
    {
        if (!validateParticipant(owner))
            return null;
        TableCard tableCard = findCard(card);
        if (tableCard == null)
            return null;
        if (tableCard.flipped)
            return null;
        tableCard.flipped = true;

        notifyAll(new FlipCard(d(), o(), card, tableCard.card));
        return tableCard.card;
    }

    public boolean moveCardOnTable(int owner, int card, int x, int y, boolean animation)
    {
        if (!validateParticipant(owner))
            return false;
        TableCard tableCard = findCard(card);
        if (tableCard == null)
            return false;
        if (!validateTable(x, y))
            return false;
        tableCard.move(x, y);

        tableCards.removeValue(tableCard, true);
        tableCards.add(tableCard);

        notifyExcept(new MoveCardOnTable(d(), o(), card, x, y, true), owner);
        notifyTo(owner, new MoveCardOnTable(d(), o(), card, x, y, animation));
        return true;
    }

    public boolean giveCardToPlayerFromTable(int owner, int card, int player, boolean animate)
    {
        if (!validateParticipant(owner))
            return false;
        TableCard tableCard = findCard(card);
        if (tableCard == null)
            return false;
        Participant participant = participants.get(player);
        if (participant == null)
            return false;
        if (!participant.give(tableCard.card))
            return false;
        tableCards.removeValue(tableCard, true);
        notifyExcept(new GiveCardToPlayerFromTable(d(), o(), player, owner, card, null, true), player);
        notifyTo(player, new GiveCardToPlayerFromTable(d(), o(), player, owner, card, tableCard.card, animate));
        return true;
    }

    public boolean giveCardToPlayerFromDeck(int owner, int player, boolean animate)
    {
        if (!validateParticipant(owner))
            return false;
        if (deck.size == 0)
            return false;
        Participant participant = participants.get(player);
        if (participant == null)
            return false;
        String card = deck.pop();
        if (!participant.give(card))
            return false;

        notifyExcept(new GiveCardToPlayerFromDeck(d(), o(), player, owner, deck.size, true), player);
        notifyTo(player, new GiveCardToPlayerFromDeck(d(), o(), player, owner, card, deck.size, animate));
        return true;
    }

    public boolean discardAllCards(int owner)
    {
        if (!validateParticipant(owner))
            return false;

        for (TableCard card : tableCards)
        {
            discard.add(card.card);
        }

        tableCards.clear();

        notifyTableToAll();
        return true;
    }

    public boolean resetGame(int owner)
    {
        if (!validateParticipant(owner))
            return false;

        for (IntMap.Entry<Participant> entry : participants)
        {
            entry.value.hand.clear();
        }

        tableCards.clear();
        discard.clear();
        deck = new Array<>(getContentComponent().getDeck());
        deck.shuffle();

        notifyTableToAll();
        return true;
    }

    public boolean discardCard(int owner, int card)
    {
        if (!validateParticipant(owner))
            return false;

        TableCard tableCard = findCard(card);
        if (tableCard == null)
            return false;
        discard.add(tableCard.card);
        tableCards.removeValue(tableCard, true);
        return true;
    }

    public boolean leave(int owner)
    {
        if (!participants.containsKey(owner))
            return false;
        Participant participant = participants.remove(owner);
        freePlaces.add(participant.place);
        return true;
    }

    public boolean join(int owner, int place)
    {
        if (participants.containsKey(owner))
            return true;

        if (!freePlaces.contains(place))
            return false;

        freePlaces.remove(place);

        Participant participant = new Participant(place);
        participants.put(owner, participant);

        notifyTableToAll();
        return true;
    }

    private String d()
    {
        ItemData itemData = ((ItemData) getComponentObject());
        return itemData.getDimension();
    }

    public int o()
    {
        ItemData itemData = ((ItemData) getComponentObject());
        return itemData.getId();
    }

    private CardsTable getTable(int for_)
    {
        ItemData itemData = ((ItemData) getComponentObject());

        return new CardsTable(itemData.getDimension(), itemData.getId(),
            getDeckSize(),
            getCardsOnTable(),
            getParticipants(for_),
            getDiscarded());
    }

    public void listen(int owner)
    {
        listeners.add(owner);

        notifyTo(owner, getTable(owner));
    }

    public void stopListening(int owner)
    {
        listeners.remove(owner);
    }

    @Override
    public void init()
    {
        super.init();

        deck.shuffle();
    }

    public int getDeckSize()
    {
        return deck.size;
    }

    public CardsTable.CardOnTable[] getCardsOnTable()
    {
        CardsTable.CardOnTable[] res = new CardsTable.CardOnTable[tableCards.size];

        for (int i = 0; i < tableCards.size; i++)
        {
            TableCard tableCard = tableCards.get(i);

            res[i] = new CardsTable.CardOnTable();

            res[i].x = tableCard.x;
            res[i].y = tableCard.y;
            res[i].card = tableCard.id;

            if (tableCard.flipped)
            {
                res[i].flipped = tableCard.card;
            }
            else
            {
                res[i].flipped = null;
            }
        }

        return res;
    }

    public CardsTable.Participant[] getParticipants(int for_)
    {
        CardsTable.Participant[] res = new CardsTable.Participant[participants.size];

        int i = 0;
        for (IntMap.Entry<Participant> participant : participants)
        {
            res[i] = new CardsTable.Participant();

            res[i].player = participant.key;
            res[i].place = participant.value.place;

            res[i].cardsCount = participant.value.hand.size;

            if (for_ == participant.key)
            {
                res[i].cards = new String[participant.value.hand.size];
                for (int j = 0; j < participant.value.hand.size; j++)
                {
                    res[i].cards[j] = participant.value.hand.get(j);
                }
            }

            i++;
        }

        return res;
    }

    public String[] getDiscarded()
    {
        String[] res = new String[discard.size];
        for (int i = 0; i < discard.size; i++)
        {
            res[i] = discard.get(i);
        }
        return res;
    }

    private void notifyTableToAll()
    {
        IntSet.IntSetIterator lit = listeners.iterator();
        while (lit.hasNext)
        {
            int listener = lit.next();
            notifyTo(listener, getTable(listener));
        }
    }

    private void notifyAll(CardMessage message)
    {
        IntSet.IntSetIterator lit = listeners.iterator();
        while (lit.hasNext)
        {
            int listener = lit.next();

            Client client = BrainOutServer.Controller.getClients().get(listener);
            if (!(client instanceof PlayerClient))
            {
                continue;
            }
            PlayerClient playerClient = ((PlayerClient) client);
            playerClient.sendTCP(message);
        }
    }

    private void notifyExcept(CardMessage message, int except)
    {
        IntSet.IntSetIterator lit = listeners.iterator();
        while (lit.hasNext)
        {
            int listener = lit.next();
            if (except == listener)
                continue;

            Client client = BrainOutServer.Controller.getClients().get(listener);
            if (!(client instanceof PlayerClient))
            {
                continue;
            }
            PlayerClient playerClient = ((PlayerClient) client);
            playerClient.sendTCP(message);
        }
    }

    private void notifyTo(int to, CardMessage message)
    {
        Client client = BrainOutServer.Controller.getClients().get(to);
        if (!(client instanceof PlayerClient))
        {
            return;
        }
        PlayerClient playerClient = ((PlayerClient) client);
        playerClient.sendTCP(message);
    }

    private static IntSet toRemove = new IntSet();

    @Override
    public void update(float dt)
    {
        super.update(dt);

        ItemData itemData = ((ItemData) getComponentObject());

        check -= dt;
        if (check < dt)
        {
            check = 5.0f;

            toRemove.clear();

            IntSet.IntSetIterator lit = listeners.iterator();
            while (lit.hasNext)
            {
                int listener = lit.next();
                Client client = BrainOutServer.Controller.getClients().get(listener);
                if (!(client instanceof PlayerClient))
                {
                    toRemove.add(listener);
                    continue;
                }
                PlayerClient playerClient = ((PlayerClient) client);
                PlayerData playerData = playerClient.getPlayerData();
                if (playerData == null)
                {
                    toRemove.add(listener);
                    continue;
                }
                if (!playerData.isAlive())
                {
                    toRemove.add(listener);
                    continue;
                }

                if (!playerData.getDimension().equals(itemData.getDimension()))
                {
                    toRemove.add(listener);
                    continue;
                }

                if (Vector2.dst2(
                    itemData.getX(),
                    itemData.getY(),
                    playerData.getX(),
                    playerData.getY()
                ) > 16.0 * 16.0)
                {
                    toRemove.add(listener);
                }
            }

            IntSet.IntSetIterator it = toRemove.iterator();
            while (it.hasNext)
            {
                int i = it.next();
                listeners.remove(i);

                if (participants.containsKey(i))
                {
                    leave(i);
                }
            }
        }
    }

    @Override
    public void release()
    {
        super.release();

        notifyAll(new LeaveTable(d(), o()));

        deck.clear();
        discard.clear();
        participants.clear();

    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
