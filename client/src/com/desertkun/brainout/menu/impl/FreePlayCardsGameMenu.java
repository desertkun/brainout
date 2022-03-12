package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.common.msg.client.cards.*;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientDeckOfCardsComponentData;
import com.desertkun.brainout.editor2.widgets.SpritesWidget;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.FreePlayCardsEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.MenuHelper;
import com.desertkun.brainout.menu.widgets.Widget;

public class FreePlayCardsGameMenu extends Menu implements EventReceiver
{
    private static int CARD_WIDTH = 40;
    private static int CARD_HEIGHT = 60;

    private static int TABLE_WIDTH = 600;
    private static int TABLE_HEIGHT = 600;

    private static int CARD_OFF_X = -CARD_WIDTH / 2;
    private static int CARD_OFF_Y = -CARD_HEIGHT / 2;

    private final ActiveData activeData;
    private final ClientDeckOfCardsComponentData deckComponent;
    private final String back;
    private Group table;
    private Group upperPlayer;
    private Group lowerPlayer;
    private Group deck;
    private DeckDragSource deckSource;
    private Group discarded;
    private DragAndDrop dragAndDrop;
    private boolean participating;
    private IntMap<CardImage> cardsOnTable;
    private IntMap<PlayerHand> hands;
    private Image ign;

    public class CardImage extends Image
    {
        private String flipped;

        public CardImage(String flipped)
        {
            super(BrainOutClient.Skin, flipped == null ? back : flipped);
            this.flipped = flipped;
        }
    }

    public class PlayerHand extends Group
    {
        private Array<String> cards;
        private int cardsCount;
        private IntMap<CardImage> images;

        public PlayerHand(String[] cards, int cardsCount)
        {
            this.cardsCount = cardsCount;

            if (cards != null)
            {
                this.cards = new Array<>(cards);
            }

            this.images = new IntMap<>();
        }

        private int getCardCount()
        {
            return cardsCount;
        }

        private String getCardAt(int idx)
        {
            if (this.cards == null)
                return null;
            return this.cards.get(idx);
        }

        public void render()
        {
            clearChildren();

            if (getCardCount() == 0)
                return;

            float space = getWidth() - CARD_WIDTH;
            int distance = (int)Math.min(space / getCardCount(), CARD_WIDTH + 4);
            float adjustedSpace = getCardCount() * distance;
            int offset = (int)((getWidth() - adjustedSpace) / 2.0f);

            for (int i = 0; i < getCardCount(); i++)
            {
                String cc = getCardAt(i);
                CardImage card = new CardImage(cc);
                card.setScaling(Scaling.none);
                card.setBounds(offset + i * distance, 0, CARD_WIDTH, CARD_HEIGHT);
                addActor(card);
                images.put(i, card);
                if (cc != null)
                {
                    dragAndDrop.addSource(new CardOnHandSource(card, getCardAt(i)));
                }
            }
        }

        public CardImage removeCard(String c)
        {
            int cardCount = getCardCount();

            if (cardCount == 0)
                return null;

            int idx;

            if (this.cards != null)
            {
                idx = cards.indexOf(c, false);
                cards.removeIndex(idx);

                if (idx < 0)
                    return null;
            }
            else
            {
                idx = MathUtils.random(0, cardCount - 1);
            }

            cardsCount--;

            CardImage toRemove = images.remove(idx);

            int updatedCardCount = cardCount - 1;

            float space = getWidth() - CARD_WIDTH;
            int distance = (int)Math.min(space / updatedCardCount, CARD_WIDTH + 4);
            float adjustedSpace = updatedCardCount * distance;
            int offset = (int)((getWidth() - adjustedSpace) / 2.0f);

            for (int i = idx + 1; i < cardCount; i++)
            {
                CardImage shift = images.remove(i);
                if (shift != null)
                {
                    images.put(i - 1, shift);
                }
            }

            for (int i = 0; i < updatedCardCount; i++)
            {
                CardImage image = images.get(i);
                if (image != null)
                {
                    image.addAction(Actions.moveTo(
                        offset + i * distance, 0, 0.25f, Interpolation.circle
                    ));
                }
            }

            return toRemove;
        }

        public CardImage addCard(String c)
        {
            int updatedCardCount = getCardCount() + 1;

            float space = getWidth() - CARD_WIDTH;
            int distance = (int)Math.min(space / updatedCardCount, CARD_WIDTH + 4);
            float adjustedSpace = updatedCardCount * distance;
            int offset = (int)((getWidth() - adjustedSpace) / 2.0f);

            int cardCount = getCardCount();
            for (int i = 0; i < cardCount; i++)
            {
                CardImage image = images.get(i);
                if (image != null)
                {
                    image.addAction(Actions.moveTo(
                        offset + i * distance, 0, 0.25f, Interpolation.circle
                    ));
                }
            }

            if (this.cards != null)
            {
                this.cards.add(c);
            }

            cardsCount++;

            CardImage card = new CardImage(c);
            card.setScaling(Scaling.none);
            card.setBounds(offset + cardCount * distance, 0, CARD_WIDTH, CARD_HEIGHT);
            images.put(cardCount, card);
            card.setVisible(false);
            addActor(card);

            if (c != null)
            {
                dragAndDrop.addSource(new CardOnHandSource(card, c));
            }

            return card;
        }
    }

    private class DeckDragSource extends DragAndDrop.Source
    {
        public DeckDragSource(Actor actor)
        {
            super(actor);
        }

        @Override
        public DragAndDrop.Payload dragStart(InputEvent inputEvent, float x, float y, int i)
        {
            DragAndDrop.Payload py = new DragAndDrop.Payload();

            {
                Group ok = new Group();

                Image card = new Image(BrainOutClient.Skin, back);
                card.setScaling(Scaling.none);
                card.setBounds(CARD_OFF_X, CARD_OFF_Y, CARD_WIDTH, CARD_HEIGHT);
                card.setAlign(Align.center);
                card.setScaling(Scaling.none);

                ok.addActor(card);
                py.setValidDragActor(ok);
            }

            {
                Group notOk = new Group();

                Image card = new Image(BrainOutClient.Skin, back);
                card.setScaling(Scaling.none);
                card.setBounds(CARD_OFF_X, CARD_OFF_Y, CARD_WIDTH, CARD_HEIGHT);
                card.setAlign(Align.center);
                card.setColor(Color.RED);
                card.setScaling(Scaling.none);

                notOk.addActor(card);
                py.setInvalidDragActor(notOk);
            }

            return py;
        }
    }

    private class CardOnHandSource extends DragAndDrop.Source
    {
        private final String card;

        public CardOnHandSource(CardImage actor, String card)
        {
            super(actor);
            this.card = card;
        }

        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer,
                             DragAndDrop.Payload payload, DragAndDrop.Target target)
        {
            CardPayload p = ((CardPayload) payload);

            if (target == null)
            {
                Vector2 c = new Vector2(payload.getInvalidDragActor().getX(), payload.getInvalidDragActor().getY());
                table.stageToLocalCoordinates(c);
                getActor().setPosition(c.x + CARD_OFF_X, c.y + CARD_OFF_Y);
                getActor().setVisible(true);
                getActor().addAction(Actions.moveTo(p.originalX, p.originalY, 0.5f, Interpolation.circle));
            }
            else
            {
                if (target instanceof TableDropTarget)
                {
                    Vector2 c = new Vector2(payload.getValidDragActor().getX(), payload.getValidDragActor().getY());
                    table.stageToLocalCoordinates(c);
                    getActor().setPosition(c.x + CARD_OFF_X, c.y + CARD_OFF_Y);
                    getActor().setVisible(true);

                    getActor().addAction(Actions.sequence(
                            Actions.delay(0.25f),
                            Actions.moveTo(p.originalX, p.originalY, 0.5f, Interpolation.circle)
                    ));
                }
            }
        }

        @Override
        public DragAndDrop.Payload dragStart(InputEvent inputEvent, float x, float y, int i)
        {
            getActor().setVisible(false);

            CardImage cardImage = ((CardImage) getActor());
            DragAndDrop.Payload py = new CardPayload(getActor());

            {
                Group ok = new Group();

                Image card = new Image(BrainOutClient.Skin, cardImage.flipped == null ? back : cardImage.flipped);
                card.setScaling(Scaling.none);
                card.setBounds(CARD_OFF_X, CARD_OFF_Y, CARD_WIDTH, CARD_HEIGHT);
                card.setAlign(Align.center);
                card.setScaling(Scaling.none);

                ok.addActor(card);
                py.setValidDragActor(ok);
            }

            {
                Group notOk = new Group();

                Image card = new Image(BrainOutClient.Skin, cardImage.flipped == null ? back : cardImage.flipped);
                card.setScaling(Scaling.none);
                card.setBounds(CARD_OFF_X, CARD_OFF_Y, CARD_WIDTH, CARD_HEIGHT);
                card.setAlign(Align.center);
                card.setColor(Color.RED);
                card.setScaling(Scaling.none);

                notOk.addActor(card);
                py.setInvalidDragActor(notOk);
            }

            return py;
        }
    }


    private class CardOnTableSource extends DragAndDrop.Source
    {
        private final int card;

        public CardOnTableSource(CardImage actor, int card)
        {
            super(actor);
            this.card = card;
        }

        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer,
                             DragAndDrop.Payload payload, DragAndDrop.Target target)
        {
            CardPayload p = ((CardPayload) payload);

            if (target == null)
            {
                Vector2 c = new Vector2(payload.getInvalidDragActor().getX(), payload.getInvalidDragActor().getY());
                table.stageToLocalCoordinates(c);
                getActor().setPosition(c.x + CARD_OFF_X, c.y + CARD_OFF_Y);
                getActor().setVisible(true);
                getActor().addAction(Actions.moveTo(p.originalX, p.originalY, 0.5f, Interpolation.circle));
            }
            else
            {
                if (target instanceof TableDropTarget)
                {
                    Vector2 c = new Vector2(payload.getValidDragActor().getX(), payload.getValidDragActor().getY());
                    table.stageToLocalCoordinates(c);
                    getActor().setPosition(c.x + CARD_OFF_X, c.y + CARD_OFF_Y);
                    getActor().setVisible(true);

                    getActor().addAction(Actions.sequence(
                        Actions.delay(0.25f),
                        Actions.moveTo(p.originalX, p.originalY, 0.5f, Interpolation.circle)
                    ));
                }
            }
        }

        @Override
        public DragAndDrop.Payload dragStart(InputEvent inputEvent, float x, float y, int i)
        {
            getActor().setVisible(false);

            CardImage cardImage = ((CardImage) getActor());
            DragAndDrop.Payload py = new CardPayload(getActor());

            {
                Group ok = new Group();

                Image card = new Image(BrainOutClient.Skin, cardImage.flipped == null ? back : cardImage.flipped);
                card.setScaling(Scaling.none);
                card.setBounds(CARD_OFF_X, CARD_OFF_Y, CARD_WIDTH, CARD_HEIGHT);
                card.setAlign(Align.center);
                card.setScaling(Scaling.none);

                ok.addActor(card);
                py.setValidDragActor(ok);
            }

            {
                Group notOk = new Group();

                Image card = new Image(BrainOutClient.Skin, cardImage.flipped == null ? back : cardImage.flipped);
                card.setScaling(Scaling.none);
                card.setBounds(CARD_OFF_X, CARD_OFF_Y, CARD_WIDTH, CARD_HEIGHT);
                card.setAlign(Align.center);
                card.setColor(Color.RED);
                card.setScaling(Scaling.none);

                notOk.addActor(card);
                py.setInvalidDragActor(notOk);
            }

            return py;
        }
    }

    private class CardPayload extends DragAndDrop.Payload
    {
        private final float originalX;
        private final float originalY;

        public CardPayload(Actor actor)
        {
            setDragActor(actor);
            this.originalX = actor.getX();
            this.originalY = actor.getY();
        }

        public void restore()
        {
            getDragActor().setPosition(originalX, originalY);
            getDragActor().setVisible(true);
        }
    }

    private class TableDropTarget extends DragAndDrop.Target
    {
        public TableDropTarget(Actor actor)
        {
            super(actor);
        }

        @Override
        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int i)
        {
            if (source instanceof CardOnHandSource)
            {
                return true;
            }

            if (source instanceof DeckDragSource)
            {
                return true;
            }

            if (source instanceof CardOnTableSource)
            {
                return true;
            }

            return false;
        }

        @Override
        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int i)
        {
            if (source instanceof CardOnHandSource)
            {
                CardOnHandSource s = ((CardOnHandSource) source);
                FreePlayCardsGameMenu.this.notify(new PlaceCardOnTableFromHand(d(), o(), -1, s.card, -1, (int)x, (int)y));
            }

            if (source instanceof DeckDragSource)
            {
                FreePlayCardsGameMenu.this.notify(new TakeCardOffDeckOntoTable(d(), o(), (int)x, (int)y));
            }

            if (source instanceof CardOnTableSource)
            {
                CardOnTableSource s = ((CardOnTableSource) source);
                FreePlayCardsGameMenu.this.notify(new MoveCardOnTable(d(), o(), s.card, (int)x, (int)y, false));
            }
        }
    }

    private class PlayerPlaceDragTarget extends DragAndDrop.Target
    {
        private final int player;

        public PlayerPlaceDragTarget(Actor actor, int player)
        {
            super(actor);
            this.player = player;
        }

        @Override
        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float v, float v1, int i)
        {
            if (source instanceof DeckDragSource)
            {
                return true;
            }

            if (source instanceof CardOnTableSource)
            {
                return true;
            }

            return false;
        }

        @Override
        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float v, float v1, int i)
        {
            if (source instanceof DeckDragSource)
            {
                FreePlayCardsGameMenu.this.notify(new GiveCardToPlayerFromDeck(d(), o(), player, false));
            }
            if (source instanceof CardOnTableSource)
            {
                CardOnTableSource s = ((CardOnTableSource) source);
                FreePlayCardsGameMenu.this.notify(new GiveCardToPlayerFromTable(d(), o(), player, -1, s.card, null, false));
            }
        }
    }

    private class IgnoreDropTarget extends DragAndDrop.Target
    {
        public IgnoreDropTarget(Actor actor)
        {
            super(actor);
        }

        @Override
        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int i)
        {
            return false;
        }

        @Override
        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int i)
        {

        }
    }

    public FreePlayCardsGameMenu(ActiveData activeData, ClientDeckOfCardsComponentData deck)
    {
        this.activeData = activeData;
        this.deckComponent = deck;
        this.back = deck.getContentComponent().getBack();
        this.dragAndDrop = new DragAndDrop();
        this.dragAndDrop.setDragTime(0);
        this.cardsOnTable = new IntMap<>();
        this.hands = new IntMap<>();
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOutClient.EventMgr.subscribe(Event.ID.freePlayCards, this);

        notify(new GetTable(d(), o()));
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());
        super.render();
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        dragAndDrop.clear();

        notify(new LeaveTable(d(), o()));
        BrainOutClient.EventMgr.unsubscribe(Event.ID.freePlayCards, this);
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        ign = new Image();
        ign.setFillParent(true);
        data.addActor(ign);
        ign.toBack();

        upperPlayer = new Group();
        upperPlayer.setSize(TABLE_WIDTH, CARD_HEIGHT);
        data.add(upperPlayer).size(TABLE_WIDTH, CARD_HEIGHT).row();

        table = new Group();
        Image bg = new Image(BrainOutClient.Skin, "form-default");
        bg.setFillParent(true);
        bg.setTouchable(Touchable.disabled);
        table.addActor(bg);
        table.setSize(TABLE_WIDTH, TABLE_HEIGHT);
        data.add(table).size(TABLE_WIDTH, TABLE_HEIGHT).row();

        lowerPlayer = new Group();
        lowerPlayer.setSize(TABLE_WIDTH, CARD_HEIGHT);
        data.add(lowerPlayer).size(TABLE_WIDTH, CARD_HEIGHT).row();

        table.toFront();

        return data;
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayCards:
            {
                FreePlayCardsEvent ev = ((FreePlayCardsEvent) event);
                final CardMessage msg = ev.msg;
                Gdx.app.postRunnable(() -> msg(msg));
                break;
            }
        }

        return false;
    }

    private String d()
    {
        return activeData.getDimension();
    }

    private int o()
    {
        return activeData.getId();
    }

    private void notify(CardMessage msg)
    {
        BrainOutClient.ClientController.sendTCP(msg);
    }

    private boolean amIParticipating(CardsTable msg)
    {
        for (CardsTable.Participant participant : msg.participants)
        {
            if (participant.player == BrainOutClient.ClientController.getMyId())
                return true;
        }

        return false;
    }

    private CardsTable.Participant findParticipation(int id, CardsTable msg)
    {
        for (CardsTable.Participant participant : msg.participants)
        {
            if (participant.place == id)
                return participant;
        }

        return null;
    }

    private void renderTable(CardsTable msg)
    {
        dragAndDrop.clear();

        dragAndDrop.addTarget(new IgnoreDropTarget(ign));
        dragAndDrop.addTarget(new TableDropTarget(table));

        table.clearChildren();

        Image bg = new Image(BrainOutClient.Skin, "form-default");
        bg.setFillParent(true);
        bg.setTouchable(Touchable.disabled);
        table.addActor(bg);

        if (msg.discarded.length > 0)
        {
            renderDiscarded(msg);
        }

        if (msg.cardsOnTable.length > 0)
        {
            renderCardsOnTable(msg);
        }

        if (msg.deckSize > 0)
        {
            renderDeck(msg);
        }

        renderPlayerPlace(upperPlayer, 0, msg);
        renderPlayerPlace(lowerPlayer, 1, msg);
    }

    private void renderCardsOnTable(CardsTable msg)
    {
        for (CardsTable.CardOnTable card : msg.cardsOnTable)
        {
            addCard(card.card, card.x, card.y, card.flipped);
        }
    }

    private void addCard(int card, int x, int y, String flipped)
    {
        CardImage c = new CardImage(flipped);
        c.setScaling(Scaling.none);
        c.setBounds(x + CARD_OFF_X, y + CARD_OFF_Y, CARD_WIDTH, CARD_HEIGHT);

        if (participating)
        {
            dragAndDrop.addSource(new CardOnTableSource(c, card));
        }

        c.addListener(new ClickListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (c.flipped == null)
                {
                    FreePlayCardsGameMenu.this.notify(new FlipCard(d(), o(), card));
                }
                else
                {
                    FreePlayCardsGameMenu.this.notify(new MoveCardOnTable(d(), o(), card,
                        TABLE_WIDTH - CARD_WIDTH + MathUtils.random(-4, 4),
                        (TABLE_HEIGHT / 2) - (CARD_HEIGHT / 2) + MathUtils.random(-4, 4), true));
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
            {
                if (button == Input.Buttons.RIGHT)
                {
                    FreePlayCardsGameMenu.this.notify(new GiveCardToPlayerFromTable(d(), o(),
                            BrainOutClient.ClientController.getMyId(), -1, card, null, true));

                    return true;
                }

                return super.touchDown(event, x, y, pointer, button);
            }
        });

        table.addActor(c);
        cardsOnTable.put(card, c);
    }

    private void renderPlayerPlace(Group player, final int id, CardsTable msg)
    {
        player.clearChildren();

        Image bg = new Image(BrainOutClient.Skin, "button-tab-normal");
        bg.setTouchable(Touchable.disabled);
        bg.setFillParent(true);
        player.addActor(bg);

        CardsTable.Participant part = findParticipation(id, msg);
        if (part == null)
        {
            if (participating)
            {
                Label empty = new Label(L.get("MENU_FREE_SEAT"), BrainOutClient.Skin, "title-gray");
                empty.setAlignment(Align.center, Align.center);
                empty.setFillParent(true);
                player.addActor(empty);
            }
            else
            {
                TextButton participate = new TextButton(L.get("MENU_TAKE_THIS_SEAT"), BrainOutClient.Skin, "button-green");
                participate.setBounds(TABLE_WIDTH / 2 - 96, CARD_HEIGHT / 2 - 16, 192, 32);
                participate.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        FreePlayCardsGameMenu.this.notify(new JoinCards(d(), o(), id));
                    }
                });
                player.addActor(participate);
            }

            dragAndDrop.addTarget(new IgnoreDropTarget(player));
        }
        else
        {
            RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(part.player);

            if (remoteClient != null)
            {
                Label nickName = new Label(remoteClient.getName(), BrainOutClient.Skin, "title-gray");
                nickName.setAlignment(Align.center, Align.center);
                nickName.setFillParent(true);
                nickName.setEllipsis(true);
                nickName.setTouchable(Touchable.disabled);
                player.addActor(nickName);

                PlayerHand hand = new PlayerHand(part.cards, part.cardsCount);
                hand.setBounds(0, 0, TABLE_WIDTH, CARD_HEIGHT);
                player.addActor(hand);
                hand.render();

                hands.put(part.player, hand);

                dragAndDrop.addTarget(new PlayerPlaceDragTarget(player, part.player));
            }
        }
    }

    private void renderDiscarded(CardsTable msg)
    {
        discarded = new Group();

        int max = Math.min(3, msg.discarded.length);
        for (int i = 0; i < max; i++)
        {
            Image back_ = new Image(BrainOutClient.Skin, msg.discarded[msg.discarded.length - 1 - i]);
            back_.setScaling(Scaling.none);
            back_.setBounds(i * 3, i * 3, CARD_WIDTH, CARD_HEIGHT);
            back_.setScaling(Scaling.none);
            back_.setTouchable(Touchable.disabled);
            discarded.addActor(back_);
        }

        table.addActor(discarded);
        discarded.setBounds(
            TABLE_WIDTH - CARD_WIDTH - 10,
            (TABLE_HEIGHT - CARD_HEIGHT) / 2,
            CARD_WIDTH,
            CARD_HEIGHT
        );
    }

    private void renderDeck(CardsTable msg)
    {
        deck = new Group();
        for (int i = 0; i < Math.min(3, msg.deckSize); i++)
        {
            Image back_ = new Image(BrainOutClient.Skin, this.back);
            back_.setScaling(Scaling.none);
            back_.setBounds(i * 3, i * 3, 40, 60);
            back_.setScaling(Scaling.none);
            back_.setTouchable(Touchable.disabled);
            deck.addActor(back_);
        }

        table.addActor(deck);
        deck.setBounds(
            10,
            (TABLE_HEIGHT - CARD_HEIGHT) / 2,
            CARD_WIDTH + 8,
            CARD_HEIGHT + 8
        );

        if (participating)
        {
            deck.addListener(new ClickListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    FreePlayCardsGameMenu.this.notify(new GiveCardToPlayerFromDeck(d(), o(),
                        BrainOutClient.ClientController.getMyId(), true));
                }
            });

            deckSource = new DeckDragSource(deck);
            dragAndDrop.addSource(deckSource);
        }
    }

    private void updateDeck(int deckSize)
    {
        if (deck == null)
            return;

        deck.clearChildren();

        for (int i = 0; i < Math.min(3, deckSize); i++)
        {
            Image back_ = new Image(BrainOutClient.Skin, this.back);
            back_.setScaling(Scaling.none);
            back_.setBounds(i * 3, i * 3, 40, 60);
            back_.setScaling(Scaling.none);
            back_.setTouchable(Touchable.disabled);
            deck.addActor(back_);
        }

        if (deckSize == 0 && participating)
        {
            if (deckSource != null)
            {
                dragAndDrop.removeSource(deckSource);
                deckSource = null;
            }
        }
    }

    @Override
    public void onFocusIn()
    {
        BrainOutClient.Env.getGameController().setControllerMode(GameController.ControllerMode.disabled);

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }
    }

    private void msg(CardMessage msg)
    {
        if (msg instanceof CardsTable)
        {
            CardsTable m = ((CardsTable) msg);
            participating = amIParticipating(m);
            renderTable(m);
        }

        if (msg instanceof TakeCardOffDeckOntoTable)
        {
            TakeCardOffDeckOntoTable m = ((TakeCardOffDeckOntoTable) msg);

            if (m.deckSize < 3)
            {
                updateDeck(m.deckSize);
            }

            if (m.animate && deck != null)
            {
                CardImage c = new CardImage(back);
                c.setScaling(Scaling.none);
                c.setBounds(deck.getX(), deck.getY(), CARD_WIDTH, CARD_HEIGHT);
                table.addActor(c);

                c.addAction(Actions.sequence(
                    Actions.moveTo(m.x + CARD_OFF_X, m.y + CARD_OFF_Y, 0.5f, Interpolation.circle),
                    Actions.run(() -> addCard(m.card, m.x, m.y, null)),
                    Actions.removeActor()
                ));
            }
            else
            {
                addCard(m.card, m.x, m.y, null);
            }
        }

        if (msg instanceof GiveCardToPlayerFromDeck)
        {
            GiveCardToPlayerFromDeck m = ((GiveCardToPlayerFromDeck) msg);
            PlayerHand hand = hands.get(m.player);
            if (hand != null && deck != null)
            {
                if (m.deckSize < 3)
                {
                    updateDeck(m.deckSize);
                }

                if (m.animate)
                {
                    CardImage c = new CardImage(m.card == null ? back : m.card);
                    c.setScaling(Scaling.none);
                    c.setBounds(deck.getX(), deck.getY(), CARD_WIDTH, CARD_HEIGHT);
                    table.addActor(c);
                    c.toFront();

                    CardImage addedCard = hand.addCard(m.card);

                    Vector2 v = new Vector2();
                    v.set(addedCard.getX(), addedCard.getY());
                    hand.localToStageCoordinates(v);
                    table.stageToLocalCoordinates(v);


                    c.addAction(Actions.sequence(
                        Actions.moveTo(v.x, v.y, 0.5f, Interpolation.circle),
                        Actions.run(() -> addedCard.setVisible(true)),
                        Actions.removeActor()
                    ));
                }
                else
                {
                    CardImage addedCard = hand.addCard(m.card);
                    addedCard.setVisible(true);
                }
            }
        }

        if (msg instanceof GiveCardToPlayerFromTable)
        {
            GiveCardToPlayerFromTable m = ((GiveCardToPlayerFromTable) msg);
            PlayerHand hand = hands.get(m.player);
            CardImage card = cardsOnTable.get(m.card);
            if (hand != null && card != null)
            {
                if (m.animate)
                {
                    CardImage addedCard = hand.addCard(m.flipped);

                    Vector2 v = new Vector2();
                    v.set(addedCard.getX(), addedCard.getY());
                    hand.localToStageCoordinates(v);
                    table.stageToLocalCoordinates(v);

                    card.addAction(Actions.sequence(
                        Actions.moveTo(v.x, v.y, 0.5f, Interpolation.circle),
                        Actions.run(() -> addedCard.setVisible(true)),
                        Actions.removeActor()
                    ));
                }
                else
                {
                    CardImage addedCard = hand.addCard(m.flipped);
                    addedCard.setVisible(true);

                    card.remove();
                    cardsOnTable.remove(m.card);
                }
            }
        }

        if (msg instanceof MoveCardOnTable)
        {
            MoveCardOnTable m = ((MoveCardOnTable) msg);
            Image card = cardsOnTable.get(m.card);
            if (card != null)
            {
                card.clearActions();
                card.setVisible(true);
                card.toFront();
                if (deck != null)
                {
                    deck.toFront();
                }
                if (m.animation)
                {
                    card.addAction(Actions.moveTo(m.x + CARD_OFF_X, m.y + CARD_OFF_Y, 0.5f, Interpolation.circle));
                }
                else
                {
                    card.setPosition(m.x + CARD_OFF_X, m.y + CARD_OFF_Y);
                }
            }
        }

        if (msg instanceof PlaceCardOnTableFromHand)
        {
            PlaceCardOnTableFromHand m = ((PlaceCardOnTableFromHand) msg);

            PlayerHand hand = hands.get(m.player);
            if (hand == null)
                return;

            if (m.player == BrainOutClient.ClientController.getMyId())
            {
                CardImage removed = hand.removeCard(m.f);
                if (removed != null)
                {
                    removed.remove();
                }

                addCard(m.card, m.x, m.y, m.f);
            }
            else
            {
                CardImage removed = hand.removeCard(m.f);

                if (removed == null)
                {
                    addCard(m.card, m.x, m.y, m.f);
                    return;
                }

                Vector2 v = new Vector2();
                v.set(removed.getX(), removed.getY());
                hand.localToStageCoordinates(v);
                table.stageToLocalCoordinates(v);

                CardImage c = new CardImage(m.f == null ? back : m.f);
                c.setScaling(Scaling.none);
                c.setBounds(v.x, v.y, CARD_WIDTH, CARD_HEIGHT);
                table.addActor(c);
                c.toFront();

                c.addAction(Actions.sequence(
                    Actions.moveTo(m.x + CARD_OFF_X, m.y + CARD_OFF_Y, 0.5f, Interpolation.circle),
                    Actions.run(() -> addCard(m.card, m.x, m.y, m.f)),
                    Actions.removeActor()
                ));

                removed.remove();
            }
        }

        if (msg instanceof FlipCard)
        {
            FlipCard m = ((FlipCard) msg);
            CardImage card = cardsOnTable.get(m.card);

            if (card != null)
            {
                card.flipped = m.flipTo;

                card.addAction(Actions.sequence(
                    Actions.parallel(
                        Actions.moveBy(CARD_WIDTH/2, 0, 0.1f, Interpolation.circleIn),
                        Actions.scaleTo(0.f, 1.f, 0.1f, Interpolation.circleIn)
                    ),
                    Actions.run(() -> card.setDrawable(BrainOutClient.Skin, m.flipTo)),
                    Actions.parallel(
                        Actions.moveBy(-CARD_WIDTH/2, 0, 0.1f, Interpolation.circleOut),
                        Actions.scaleTo(1.f, 1.f, 0.1f, Interpolation.circleOut)
                    )
                ));

            }
        }

        if (msg instanceof LeaveTable)
        {
            pop();
        }
    }
}
