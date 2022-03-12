package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.L;
import com.desertkun.brainout.content.battlepass.BattlePass;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.MenuHelper;
import com.desertkun.brainout.online.IAP;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.services.StoreService;

import java.text.DecimalFormat;
import java.util.HashMap;

public class PurchaseBattlePassMenu extends Menu implements EventReceiver
{
    private static DecimalFormat PriceFormat = new DecimalFormat("#.##");
    private final BattlePass battlePass;

    public PurchaseBattlePassMenu(BattlePass battlePass)
    {
        this.battlePass = battlePass;
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
        data.align(Align.top);

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-red");

            Label brain = new Label("BRAIN", BrainOutClient.Skin, "title-yellow");
            header.add(brain).pad(8);

            Image bp = new Image(BrainOutClient.Skin, "icon-bp-premium");
            bp.setScaling(Scaling.none);
            bp.setScale(2);
            bp.setOrigin(9, 9);
            header.add(bp).size(18).pad(4);

            Label pass = new Label("PASS", BrainOutClient.Skin, "title-small");
            header.add(pass).pad(8);

            data.add(header).expandX().fill().pad(8).padTop(104).padBottom(32).row();
        }

        {
            Table entry = new Table(BrainOutClient.Skin);
            entry.setBackground("label-purple");
            Label seasonIncludes = new Label(L.get("MENU_BP_SEASON_INCLUDES"), BrainOutClient.Skin, "title-small");
            seasonIncludes.setAlignment(Align.center);
            entry.add(seasonIncludes).expand().fill().padLeft(128).padRight(128);
            data.add(entry).expandX().fill().height(128).pad(-96).expandX().center().row();
        }

        {
            Table description = new Table();

            {
                Image label = new Image(BrainOutClient.Skin, "icon-battlepass-x2-boost");
                label.setScaling(Scaling.none);
                description.add(label).size(340, 192).pad(32).padLeft(96).padTop(0);
            }

            {
                Table descr = new Table();
                descr.align(Align.top);

                {
                    Image check = new Image(BrainOutClient.Skin, "icon-checkmark");
                    descr.add(check).pad(4).padRight(16);

                    Label d = new Label(L.get("MENU_BP_REWARD_MTS_569"), BrainOutClient.Skin, "title-small");
                    d.setWrap(true);
                    descr.add(d).pad(4).expandX().left().row();
                }

                {
                    Image check = new Image(BrainOutClient.Skin, "icon-checkmark");
                    descr.add(check).pad(4).padRight(16);

                    Label d = new Label(L.get("MENU_BP_REWARD_ITHACA_37"), BrainOutClient.Skin, "title-small");
                    d.setWrap(true);
                    descr.add(d).pad(4).expandX().left().row();
                }

                {
                    Image check = new Image(BrainOutClient.Skin, "icon-checkmark");
                    descr.add(check).pad(4).padRight(16);

                    Label d = new Label(L.get("MENU_BP_REWARD_RFE"), BrainOutClient.Skin, "title-small");
                    d.setWrap(true);
                    descr.add(d).pad(4).expandX().left().row();
                }

                {
                    Image check = new Image(BrainOutClient.Skin, "icon-checkmark");
                    descr.add(check).pad(4).padRight(16);

                    Table d = new Table();

                    Label v1 = new Label("+1000", BrainOutClient.Skin, "title-small");
                    d.add(v1);
                    Image bp = new Image(BrainOutClient.Skin, "icon-battle-pass-points");
                    bp.setScaling(Scaling.none);
                    d.add(bp).padLeft(8);

                    descr.add(d).pad(4).expandX().left().row();
                }

                {
                    Image check = new Image(BrainOutClient.Skin, "icon-checkmark");
                    descr.add(check).pad(4).padRight(16);

                    Label d = new Label(L.get("MENU_BP_ADDITIONAL_DAILY_BATTLE_TASK"), BrainOutClient.Skin, "title-small");
                    d.setWrap(true);
                    descr.add(d).pad(4).expandX().left().row();
                }

                {
                    Image check = new Image(BrainOutClient.Skin, "icon-checkmark");
                    descr.add(check).pad(4).padRight(16);

                    Label d = new Label(L.get("MENU_BP_UNIQUE_NICKNAME_COLOR"), BrainOutClient.Skin, "title-small");
                    d.setWrap(true);
                    descr.add(d).pad(4).expandX().fillX().left().row();
                }

                {
                    Image check = new Image(BrainOutClient.Skin, "icon-checkmark");
                    descr.add(check).pad(4).padRight(16);

                    Label d = new Label(L.get("MENU_BP_PREMIUM_REWARDS", "50"), BrainOutClient.Skin, "title-small");
                    d.setWrap(true);
                    descr.add(d).pad(4).expandX().left().row();
                }

                description.add(descr).pad(32).padTop(0).expand().fill().row();
            }

            data.add(description).padTop(48).expandX().fillX().row();

        }

        {
            Label generalDescription = new Label(L.get("MENU_BP_DESCRIPTION"), BrainOutClient.Skin, "title-small");
            generalDescription.setWrap(true);
            generalDescription.setAlignment(Align.center);
            data.add(generalDescription).width(800).pad(32).expandX().center().row();
        }

        {
            Table purchaseForm = new Table();

            requestStore(purchaseForm);

            data.add(purchaseForm).width(192).height(128).pad(32).row();
        }

        return data;
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
                        if (item.getId().equals(battlePass.getID()))
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
        if (battlePass.hasItem(BrainOutClient.ClientController.getUserProfile(), false))
        {
            Table activated = new Table(BrainOutClient.Skin);
            activated.setBackground("form-gray");
            Label title = new Label(L.get("MENU_BP_ACTIVATED"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            activated.add(title).expand().fill();
            purchaseForm.add(activated).size(192, 32).padBottom(32).row();
            return;
        }

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-gray");

            Label buy = new Label(L.get("MENU_MARKET_BUY"), BrainOutClient.Skin, "title-yellow");
            header.add(buy);

            purchaseForm.add(header).expandX().fillX().row();
        }

        StoreService.Store.Tier.Price price = getPrice(item);
        double totalPrice = (double) price.getPrice() / 100.d;

        {
            TextButton buy = new TextButton(
                price.getFormat().replace("{0}", PriceFormat.format(totalPrice)),
                BrainOutClient.Skin, "button-green");
            buy.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    purchase(item);
                }
            });

            purchaseForm.add(buy).height(64).expand().fillX().top().row();
        }
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
                store, itemName, 1, currency, component, env);

        pushMenu(new NewOrderResultMenu("")
        {
            @Override
            protected void userProfileUpdated()
            {
                profileUpdated();
            }
        });
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

        MenuHelper.AddCloseButton(this, this::pop);

        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);
    }

    @Override
    public boolean lockUpdate()
    {
        return true;
    }

    @Override
    public boolean escape()
    {
        pop();
        return true;
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-battlepass");
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                SimpleEvent se = ((SimpleEvent) event);

                if (se.getAction() == SimpleEvent.Action.userProfileUpdated)
                {
                    profileUpdated();
                }

                break;
            }
        }

        return false;
    }

    private void profileUpdated()
    {
        if (Log.INFO) Log.info("Purchase menu profile updated");
        if (BrainOutClient.ClientController.getUserProfile().hasItem(battlePass, false))
        {
            if (Log.INFO) Log.info("popped!");
            BrainOutClient.getInstance().topState().popMenu(this);
        }
    }
}
