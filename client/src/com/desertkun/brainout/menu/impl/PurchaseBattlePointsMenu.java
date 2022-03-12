package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.battlepass.BattlePass;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.OnlineEventUpdatedEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.MenuHelper;
import com.desertkun.brainout.online.ClientBattlePassEvent;
import com.desertkun.brainout.online.IAP;
import org.anthillplatform.runtime.services.StoreService;

import java.text.DecimalFormat;
import java.util.HashMap;

public class PurchaseBattlePointsMenu extends Menu implements EventReceiver
{
    private static DecimalFormat PriceFormat = new DecimalFormat("#.##");
    private final int maxLevel;
    private final int eventId;
    private Label bpValue;
    private int addLevels = 1;
    private TextButton purchase;
    private double totalPrice;
    private String priveFormat;

    public PurchaseBattlePointsMenu(int eventId, int maxLevel)
    {
        this.eventId = eventId;
        this.maxLevel = maxLevel;
    }

    private void requestStore(Table purchaseForm)
    {
        IAP.GetStore("battle-pass", new IAP.StoreCallback()
        {
            @Override
            public void succeed(StoreService.Store store)
            {
                Gdx.app.postRunnable(() ->
                {
                    for (StoreService.Store.Item item : store.getItems())
                    {
                        if (item.getId().equals("battle-points"))
                        {
                            renderBuyButton(purchaseForm, item);
                            return;
                        }
                    }

                    purchaseForm.clearChildren();
                    Image error = new Image(BrainOutClient.Skin, "icon-boost-bleeding");
                    error.setScaling(Scaling.none);
                    purchaseForm.add(error).pad(16);
                });
            }

            @Override
            public void failed()
            {
                Gdx.app.postRunnable(() ->
                {
                    purchaseForm.clearChildren();
                    Image error = new Image(BrainOutClient.Skin, "icon-boost-bleeding");
                    error.setScaling(Scaling.none);
                    purchaseForm.add(error).pad(16);
                });
            }
        });
    }

    private StoreService.Store.Tier.Price getPrice(StoreService.Store.Item item)
    {
        StoreService.Store.Item.Billing billing = item.getBilling();

        return billing.getTier().getPrices().getOrDefault(
                BrainOutClient.Env.getUserCurrency(),
                billing.getTier().getPrices().get(BrainOutClient.Env.getDefaultCurrency()));
    }

    private void renderBuyButton(Table purchaseForm, StoreService.Store.Item item)
    {
        purchaseForm.clearChildren();

        StoreService.Store.Tier.Price price = getPrice(item);
        priveFormat = price.getFormat();
        totalPrice = (double) price.getPrice() / 100.d;

        purchase = new TextButton(
            priveFormat.replace("{0}", PriceFormat.format(totalPrice)),
            BrainOutClient.Skin, "button-green");
        purchase.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);
                purchase(item);
            }
        });

        purchaseForm.add(purchase).size(192, 64);
    }

    private void purchase(StoreService.Store.Item item)
    {
        if (!BrainOutClient.Env.storeEnabled())
        {
            Menu.playSound(MenuSound.denied);
            return;
        }

        Menu.playSound(MenuSound.select);

        StoreService.Store.Tier.Price price = getPrice(item);

        if (price == null)
        {
            return;
        }

        String component = BrainOutClient.Env.getStoreComponent();
        String store = item.getStore().getName();
        String currency = price.getCurrency();
        String itemName = item.getId();

        HashMap<String, String> env = new HashMap<>();
        BrainOutClient.Env.getStoreEnvironment(env);

        BrainOutClient.ClientController.createNewOrder(
            store, itemName, addLevels, currency, component, env);

        pushMenu(new NewOrderResultMenu("")
        {
            @Override
            protected void userProfileUpdated()
            {
            }
        });
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();
        Table root = new Table(BrainOutClient.Skin);
        root.setBackground("form-default");
        root.align(Align.top);

        {
            Table img = new Table();
            Image bg = new Image(BrainOutClient.Skin, "store-bp-points");
            img.add(bg).row();
            Label title = new Label(L.get("MENU_BP_BATTLE_POINTS_PACK"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            img.add(title).expandX().fillX().padTop(-34).height(34);
            root.add(img).pad(8).row();
        }

        {
            Label description = new Label(L.get("MENU_BP_BATTLE_POINTS_PACK_DESC"), BrainOutClient.Skin, "title-small");
            description.setAlignment(Align.center);
            description.setWrap(true);
            root.add(description).expand().fill().pad(16).padLeft(64).padRight(64).row();
        }

        {
            Table p = new Table();

            {
                Table purchaseTable = new Table();
                p.add(purchaseTable).expandX().uniformX().fillX();
                requestStore(purchaseTable);
            }

            {
                Table sec2 = new Table();
                p.add(sec2).expandX().uniformX().fillX();

                TextButton minus = new TextButton("-", BrainOutClient.Skin, "button-fill");
                minus.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        if (addLevels > 1)
                        {
                            addLevels--;

                            refreshLevels();
                        }
                    }
                });
                sec2.add(minus).size(32, 64);

                {
                    Table ddd = new Table(BrainOutClient.Skin);
                    ddd.setBackground("form-default");

                    Image bp = new Image(BrainOutClient.Skin, "icon-battle-pass-points");
                    ddd.add(bp).expandX().center().row();

                    bpValue = new Label("1000", BrainOutClient.Skin, "title-small");
                    bpValue.setAlignment(Align.center);
                    ddd.add(bpValue).expandX().fill().padLeft(8).padRight(8).row();

                    sec2.add(ddd).width(128).expandY().fill();
                }

                TextButton plus = new TextButton("+", BrainOutClient.Skin, "button-fill");
                plus.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        if (addLevels < maxLevel)
                        {
                            addLevels++;
                            refreshLevels();
                        }
                    }
                });
                sec2.add(plus).size(32, 64);
            }


            root.add(p).expandX().fill().pad(8).row();
        }

        data.add(root).size(608, 400).row();
        return data;
    }

    private void refreshLevels()
    {
        bpValue.setText(addLevels * 1000);
        if (purchase != null)
        {
            purchase.setText(priveFormat.replace("{0}", PriceFormat.format(addLevels * totalPrice)));
        }
    }

    @Override
    public boolean lockUpdate()
    {
        return true;
    }

    @Override
    public void onInit()
    {
        super.onInit();
        MenuHelper.AddCloseButton(this, this::pop);
        BrainOutClient.EventMgr.subscribe(Event.ID.onlineEventUpdated, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();
        BrainOutClient.EventMgr.unsubscribe(Event.ID.onlineEventUpdated, this);
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
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.center;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case onlineEventUpdated:
            {
                OnlineEventUpdatedEvent ev = ((OnlineEventUpdatedEvent) event);

                if (ev.event.getEvent().id == eventId)
                {
                    if (ev.event instanceof ClientBattlePassEvent)
                    {
                        pop();
                        break;
                    }
                }

                break;
            }
        }

        return false;
    }
}
