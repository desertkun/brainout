package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.common.msg.server.CaseOpenResultMsg;
import com.desertkun.brainout.content.Sound;
import com.desertkun.brainout.content.components.ClientCardComponent;
import com.desertkun.brainout.content.components.ClientCardGroupComponent;
import com.desertkun.brainout.content.components.ClientCaseComponent;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.gamecase.CardGroup;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.content.gamecase.gen.Card;
import com.desertkun.brainout.data.gamecase.CaseData;
import com.desertkun.brainout.data.gamecase.gen.CardData;
import com.desertkun.brainout.events.CaseOpenResultEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.NotifyEvent;
import com.desertkun.brainout.gs.actions.MenuAction;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.ActionList;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.LoadingBlock;

public class OpenCaseMenu extends Menu implements EventReceiver
{
    private CaseData caseData;
    private Table data;
    private Table cardsContainer;
    private ActionList actionList = new ActionList();
    private Array<CardActor> cards = new Array<>();
    private Array<Event> delayedEvents = new Array<>();
    private Table buttons;

    public class CardActor extends Group
    {
        private final CardData cardData;
        private boolean flipped;

        public CardActor(CardData cardData)
        {
            this.cardData = cardData;
            this.flipped = false;

            init();
        }

        private void init()
        {
            CardGroup cardGroup = cardData.getCard().getGroup();
            IconComponent cardGroupIcon = cardGroup.getComponent(IconComponent.class);

            setTransform(true);
            setOrigin(96, 128);

            Image bg = new Image(cardGroupIcon.getIcon("icon-back"));
            bg.setFillParent(true);

            addActor(bg);
            setColor(1, 1, 1, 0);

            addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    flip();
                }
            });
        }

        private void flip()
        {
            if (this.flipped)
                return;

            this.flipped = true;

            clearListeners();

            actionList.addAction(new MenuAction()
            {
                @Override
                public void run()
                {
                    CardGroup cardGroup = cardData.getCard().getGroup();
                    ClientCardGroupComponent ccg = cardGroup.getComponent(ClientCardGroupComponent.class);
                    ClientCardComponent ccc = cardData.getCard().getComponentFrom(ClientCardComponent.class);

                    if (ccg != null)
                    {
                        ccg.getFlipEffect().play();
                    }

                    if (ccc != null)
                    {
                        ccc.flip(cardData, CardActor.this);
                    }

                    addAction(Actions.sequence(
                        Actions.scaleTo(0, 1, 0.125f),
                        Actions.run(CardActor.this::flipped),
                        Actions.scaleTo(1, 1, 0.125f),
                        Actions.run(() ->
                        {
                            int count = 0;

                            for (CardActor actor : cards)
                            {
                                if (actor.flipped)
                                {
                                    count++;
                                }
                            }

                            if (count == cards.size)
                            {
                                allCardsFlipped();
                            }

                            done();
                        })
                    ));
                }
            });
        }

        private void flipped()
        {
            clearChildren();

            Card card = cardData.getCard();
            CardGroup cardGroup = card.getGroup();

            ClientCardGroupComponent ccg = cardGroup.getComponent(ClientCardGroupComponent.class);
            IconComponent cardGroupIcon = cardGroup.getComponent(IconComponent.class);
            ClientCardComponent ccc = card.getComponentFrom(ClientCardComponent.class);

            TextureRegion shine = cardGroupIcon.getIcon("icon-shine");
            if (shine != null)
            {
                Image bg = new Image(shine);
                bg.setScaling(Scaling.none);
                bg.setFillParent(true);
                bg.getColor().a = 0;

                bg.addAction(Actions.sequence(
                    Actions.alpha(1, 0.5f),
                    Actions.delay(1.0f),
                    Actions.alpha(0, 0.5f)
                ));

                addActor(bg);
            }

            Image bg = new Image(cardGroupIcon.getIcon("icon-bg"));
            bg.setFillParent(true);

            addActor(bg);

            Table cardContent = new Table();
            cardContent.setFillParent(true);

            Label title = new Label(ccc != null ? ccc.getDescription(cardData) : "", BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            title.setWrap(true);
            cardContent.add(title).pad(14).expandX().fillX().row();

            if (ccc != null)
            {
                ccc.drawIcon(cardContent, cardData);
            }

            Label description = new Label(ccc != null ? ccc.getTitle(cardData) : "",
                    BrainOutClient.Skin, "title-small");
            description.setWrap(true);
            description.setAlignment(Align.center);
            cardContent.add(description).pad(4).row();

            Label category = new Label(ccc.getGroupTitle(cardData), BrainOutClient.Skin, ccg.getTitleStyle());
            category.setAlignment(Align.center);
            cardContent.add(category).expandX().fillX().bottom().pad(8).row();

            addActor(cardContent);
        }

        public void open()
        {
            CardGroup cardGroup = cardData.getCard().getGroup();
            ClientCardGroupComponent ccg = cardGroup.getComponent(ClientCardGroupComponent.class);

            actionList.addAction(new MenuAction()
            {
                @Override
                public void run()
                {
                    for (Sound sound : ccg.getOpenEffect())
                    {
                        sound.play();
                    }

                    addAction(Actions.sequence(
                            Actions.alpha(1.0f, 0.25f),
                            Actions.run(this::done)
                    ));
                }
            });
        }
    }

    @Override
    public Table createUI()
    {
        this.data = new Table();
        data.align(Align.right | Align.bottom);

        data.add(new LoadingBlock()).pad(32);

        return data;
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOutClient.EventMgr.subscribeAt(Event.ID.notify, this, true);
        BrainOutClient.EventMgr.subscribe(Event.ID.caseOpenResult, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.notify, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.caseOpenResult, this);

        for (Event event : delayedEvents)
        {
            BrainOutClient.EventMgr.sendDelayedEvent(event);
        }

        delayedEvents.clear();
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(0.75f, getBatch());

        super.render();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case notify:
            {
                NotifyEvent notify = (NotifyEvent) event;

                Event copy = NotifyEvent.obtain(
                    notify.notifyAward,
                    notify.amount,
                    notify.reason,
                    notify.method,
                    notify.data
                );

                delayedEvents.add(copy);

                return true;
            }
            case caseOpenResult:
            {
                CaseOpenResultEvent e = (CaseOpenResultEvent) event;

                onCaseResult(e.result, e.caseData);

                return true;
            }
        }

        return false;
    }

    private void onCaseResult(CaseOpenResultMsg.Result result, CaseData caseData)
    {
        switch (result)
        {
            case success:
            {
                this.caseData = caseData;

                processCase();

                break;
            }
            default:
            {
                pop();
                failed(result);

                break;
            }
        }
    }

    private void failed(CaseOpenResultMsg.Result result)
    {
        switch (result)
        {
            case notApplicable:
            {
                pushMenu(new AlertPopup(L.get("MENU_CASE_IS_NOT_APPLICABLE")));

                break;
            }
        }
    }

    private void processCase()
    {
        this.data.clear();
        this.data.align(Align.center);
        this.cards.clear();

        this.cardsContainer = new Table();
        this.buttons = new Table();

        Case gameCase = ((Case) caseData.getContent());
        if (gameCase.hasComponent(ClientCaseComponent.class))
        {
            ClientCaseComponent ccc = gameCase.getComponent(ClientCaseComponent.class);

            ccc.getOpenEffect().play();
        }

        gameCase.getCards().shuffle();

        for (CaseData.CardResult result : caseData.getCards())
        {
            CardData cardData = result.cardData;
            if (cardData == null || cardData.getCard() == null)
                continue;
            cards.add(new CardActor(cardData));
        }

        cards.shuffle();

        int cnt = 0;
        Table row = new Table();
        cardsContainer.add(row).row();

        int pad = cards.size >= 5 ? 3 : 6;

        for (CardActor cardActor : cards)
        {
            row.add(cardActor).size(192, 256).pad(32).padTop(8).padBottom(8);

            cnt++;

            if (cnt % pad == 0)
            {
                row = new Table();
                cardsContainer.add(row).row();
                cnt = 0;
            }
        }

        for (CardActor cardActor : cards)
        {
            cardActor.open();
        }

        actionList.addAction(new MenuAction()
        {
            @Override
            public void run()
            {
                buttons.clear();

                TextButton openAll = new TextButton(L.get("MENU_OPEN_ALL"),
                    BrainOutClient.Skin, "button-default");

                openAll.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        flipAll();
                    }
                });

                buttons.add(openAll).size(192, 64);

                done();
            }
        });

        this.data.add(cardsContainer).pad(32).row();
        this.data.add(buttons).height(64).row();
    }

    private void allCardsFlipped()
    {
        buttons.clear();

        TextButton collect = new TextButton(L.get("MENU_COLLECT"),
                BrainOutClient.Skin, "button-default");

        collect.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.equip);
                pop();
            }
        });

        buttons.add(collect).size(192, 64);
    }

    private void flipAll()
    {
        for (CardActor card : cards)
        {
            card.flip();
        }
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        actionList.processActions(delta);
    }
}
