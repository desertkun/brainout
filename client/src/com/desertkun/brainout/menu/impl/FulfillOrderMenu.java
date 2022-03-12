package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.ConsumableND;
import com.desertkun.brainout.content.active.PersonalContainer;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.ItemComponent;
import com.desertkun.brainout.content.components.MaxWeightComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.NotifyEvent;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.LabeledSlider;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.ContentImage;
import com.desertkun.brainout.utils.MarketUtils;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.MarketService;
import org.json.JSONObject;

public class FulfillOrderMenu extends FormMenu
{
    private final Runnable bought;
    private final MarketService.MarketOrderEntry entry;
    private final ConsumableRecord record;
    private final String category;
    private TextButton buy;
    private Label total;
    private int pieces;
    private int min;
    private int max;
    private int price;
    private int amount;
    private int ruAvailable;
    private float maxWeight;
    private float itemWeight;
    private float existingItemsWeight;
    private Label ruAvailableLabel;
    private Label totalWeight;

    public FulfillOrderMenu(MarketService.MarketOrderEntry entry, ConsumableRecord record, Runnable bought, String category)
    {
        this.entry = entry;
        this.record = record;
        this.category = category;

        if (record.getItem().getContent() instanceof Bullet)
        {
            pieces = ((Bullet) record.getItem().getContent()).getGood();
            min = pieces;
        }
        else
        {
            pieces = 1;
            min = 1;
        }

        max = entry.available * entry.giveAmount;
        price = entry.takeAmount;
        amount = max;

        if (calculateCreationPrice() > getRUAvailable())
        {
            amount = Math.max(min, (getRUAvailable() / price) * pieces);
        }

        ItemComponent item = record.getItem().getContent().getComponent(ItemComponent.class);
        if (item != null)
        {
            itemWeight = item.getWeight();
        }

        this.bought = bought;
    }

    private int getRUAvailable()
    {
        return ruAvailable;
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }
    }

    @Override
    public void onInit()
    {
        super.onInit();

        PersonalContainer personalContainer = BrainOut.ContentMgr.get("personal-container", PersonalContainer.class);

        UserProfile profile = BrainOutClient.ClientController.getUserProfile();
        maxWeight = MarketUtils.GetMaxMarketWeightForPlayer(personalContainer, profile,
            MarketUtils.GetMarketItemCategory(entry.giveItem));

        MarketService marketService = MarketService.Get();
        LoginService loginService = LoginService.Get();

        if (marketService != null && loginService != null)
        {
            marketService.getMarketItems("freeplay", loginService.getCurrentAccessToken(),
                (request, result, entries) -> Gdx.app.postRunnable(() ->
            {
                existingItemsWeight = 0;

                for (MarketService.MarketItemEntry entry : entries)
                {
                    if (entry.name.equals("ru"))
                    {
                        ruAvailable = entry.amount;
                        continue;
                    }

                    String category = MarketUtils.GetMarketItemCategory(entry.name);
                    if (!this.category.equals(category))
                    {
                        continue;
                    }

                    ConsumableRecord r = MarketUtils.MarketObjectToConsumableRecord(
                        entry.name, entry.payload, entry.amount);

                    if (r == null)
                        continue;

                    ItemComponent i = r.getItem().getContent().getComponent(ItemComponent.class);
                    if (i == null)
                        continue;

                    existingItemsWeight += i.getWeight() * entry.amount;
                }

                addRUButton(ruAvailable);
                updateFee();
                calculateTotal();
            }));
        }
    }

    private void addRUButton(int amount)
    {
        Button ru = new Button(BrainOutClient.Skin, "button-notext");

        Table icon = new Table();
        ContentImage.RenderStatImage("ru", amount, icon);
        ru.add(icon).row();

        ruAvailableLabel = new Label(String.valueOf(amount), BrainOutClient.Skin, "title-small");
        ruAvailableLabel.setAlignment(Align.center);
        ru.add(ruAvailableLabel).expandX().fillX().row();

        ru.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                addMoreRU();
            }
        });

        ru.setBounds(BrainOutClient.getWidth() - 212 - 64, BrainOutClient.getHeight() - 84, 192, 64);
        Tooltip.RegisterStandardToolTip(ru,
            L.get("MENU_MARKET_RU_BALANCE"), L.get("MENU_MARKET_RU_BALANCE_DESC"), this);

        addActor(ru);


        Button ex = new Button(BrainOutClient.Skin, "button-green");
        Image exIcon = new Image(BrainOutClient.Skin, "icon-exchange-ru");
        exIcon.setScaling(Scaling.none);
        ex.add(exIcon).expand().fill();

        ex.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);
                addMoreRU();
            }
        });

        ex.setBounds(BrainOutClient.getWidth() - 64 - 16, BrainOutClient.getHeight() - 84, 64, 64);

        addActor(ex);
    }

    private int getProfileRU()
    {
        return BrainOutClient.ClientController.getUserProfile().getInt("ru", 0);
    }

    private void addMoreRU()
    {
        int need = calculateCreationPrice() - ruAvailable;
        int haveRu = getProfileRU();

        if (haveRu == 0 || haveRu < need)
        {
            pushMenu(new AlertPopup(L.get("MENU_NOT_ENOUGH_RU")));
            return;
        }

        pushMenu(new TransferRUMenu(Math.max(need, 1),
                haveRu, true, ruAvailable, haveRu, new TransferRUMenu.Callback()
        {

            @Override
            public void approve(int amount)
            {
                ruAvailable = amount;
                ruAvailableLabel.setText(amount);
                updateFee();

                Menu.playSound(MenuSound.itemSold);
            }

            @Override
            public void cancel()
            {
                Menu.playSound(MenuSound.back);
            }
        }));
    }

    @Override
    public boolean escape()
    {
        pop();
        return true;
    }

    @Override
    public Table createUI()
    {
        buy = new TextButton(L.get("MENU_BUY_FOR_RU", "..."), BrainOutClient.Skin, "button-green");

        Table body = new Table();

        {
            Table header = new Table();
            header.setSkin(BrainOutClient.Skin);
            header.setBackground("form-red");

            {
                Label title = new Label(L.get("MENU_MARKET_BUY_ITEM"), BrainOutClient.Skin, "title-yellow");
                header.add(title).expandX().center().row();
            }

            body.add(header).expandX().fillX().row();
        }
        {
            Table data = new Table();
            data.setSkin(BrainOutClient.Skin);
            data.setBackground(formBorderStyle());
            data.align(Align.center);

            Table payload = new Table();
            renderPayload(payload);

            data.add(payload).minWidth(400).pad(8).row();

            body.add(data).expand().fill().row();
        }
        {
            Table buttons = new Table();

            {
                TextButton cancel = new TextButton(L.get("MENU_CANCEL"), BrainOutClient.Skin, "button-small");
                cancel.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.back);
                        pop();
                    }
                });
                cancel.getLabel().setWrap(true);
                buttons.add(cancel).size(256, 64).fill();
            }
            {
                buy.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        if (!isEnoughRU())
                        {
                            addMoreRU();
                            return;
                        }

                        if (getItemsWeight() > maxWeight)
                        {
                            Menu.playSound(MenuSound.denied);
                            return;
                        }

                        fulfillOrder();
                    }
                });
                buy.getLabel().setWrap(true);
                buttons.add(buy).size(256, 64).fill();
            }

            body.add(buttons).expandX().fillX().height(64).row();
        }

        return body;
    }

    private boolean isEnoughRU()
    {
        return getRUAvailable() >= calculateCreationPrice();
    }

    private void fulfillOrder()
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();

        args.put("market", "freeplay");
        args.put("item_name", entry.giveItem);
        args.put("order_id", entry.orderId);
        args.put("amount", amount / pieces);

        BrainOutClient.SocialController.sendRequest("fulfill_market_order", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();

                BrainOutClient.EventMgr.sendDelayedEvent(NotifyEvent.obtain(
                    NotifyAward.consumable, 1, NotifyReason.marketItemPurchased, NotifyMethod.message,
                    new ConsumableND(record)
                ));

                pop();

                bought.run();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.popMeAndPushMenu(
                    new AlertPopup(L.get(reason)));
            }
        });

    }

    private int calculateCreationPrice()
    {
        return price * (amount / pieces);
    }

    private void renderPayload(Table payload)
    {
        Label priceFor1 = new Label(pieces == 1 ? L.get("MENU_PRICE_FOR_1_PIECE") :
            L.get("MENU_PRICE_FOR_N_PIECES", String.valueOf(pieces)), BrainOutClient.Skin, "title-yellow");

        payload.add(priceFor1).pad(4).expandX().center().row();

        {
            Table item = new Table();

            {
                Table r1 = new Table(BrainOutClient.Skin);
                r1.setBackground("form-default");

                if (record.getItem() instanceof InstrumentConsumableItem)
                {
                    InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
                    if (ici.getInstrumentData().getInstrument().getComponent(IconComponent.class) != null)
                    {
                        ContentImage.RenderImage(ici.getInstrumentData().getInstrument(), r1, 1);
                    }
                    else
                    {
                        ContentImage.RenderInstrument(r1, ici.getInstrumentData().getInfo());
                    }
                }
                else
                {
                    ContentImage.RenderImage(record.getItem().getContent(), r1, 1);
                }

                item.add(r1).size(192, 64);
            }
            {
                Table r2 = new Table(BrainOutClient.Skin);
                r2.setBackground("form-default");

                Label price_ = new Label(String.valueOf(price) + " RU", BrainOutClient.Skin, "title-small");
                price_.setAlignment(Align.center);
                r2.add(price_).expand().fill();
                item.add(r2).size(192, 64);
            }

            payload.add(item).expandX().fillX().row();
        }


        if (min != max)
        {
            LabeledSlider amount_ = new LabeledSlider(amount, min, max, pieces)
            {
                @Override
                protected void onChanged(int newValue)
                {
                    super.onChanged(newValue);

                    amount = newValue;

                    updateFee();
                    calculateTotal();
                }
            };

            payload.add(amount_).expand().fillX().pad(16).padBottom(0).row();
        }

        {
            Table total_ = new Table();

            Label t = new Label(L.get("MENU_MARKET_TOTAL"), BrainOutClient.Skin, "title-yellow");
            total_.add(t).row();

            total = new Label("", BrainOutClient.Skin, "title-small");
            total_.add(total).row();

            payload.add(total_).pad(16).row();
        }

        ItemComponent item = record.getItem().getContent().getComponent(ItemComponent.class);
        if (item != null)
        {
            Table weight_ = new Table();

            Label t = new Label(L.get("MENU_MARKET_WEIGHT_VS_AVAILABLE"), BrainOutClient.Skin, "title-yellow");
            weight_.add(t).row();

            totalWeight = new Label("...", BrainOutClient.Skin, "title-small");
            weight_.add(totalWeight).row();

            payload.add(weight_).pad(16).padTop(0).row();
        }

        calculateTotal();
        updateFee();
    }

    private float getItemsWeight()
    {
        return existingItemsWeight + amount * itemWeight;
    }

    private void calculateTotal()
    {
        total.setText(calculateCreationPrice() + " RU");

        if (totalWeight != null && maxWeight > 0)
        {
            totalWeight.setText(String.format("%.2f", amount * itemWeight) + " / " +
                    String.format("%.2f", maxWeight - existingItemsWeight));

            totalWeight.setStyle(BrainOutClient.Skin.get(getItemsWeight() > maxWeight ? "title-red" : "title-small",
                Label.LabelStyle.class));
        }
    }

    private void updateFee()
    {
        if (!isEnoughRU())
        {
            buy.setText(L.get("MENU_NOT_ENOUGH_RU"));
            buy.setDisabled(true);
            return;
        }

        if (totalWeight != null)
        {
            buy.setDisabled(getItemsWeight() > maxWeight);

            if (buy.isDisabled())
            {
                buy.setText("...");
            }
            else
            {
                buy.setText(L.get("MENU_BUY_FOR_RU", String.valueOf(calculateCreationPrice())));
            }
        }
        else
        {
            buy.setText(L.get("MENU_BUY_FOR_RU", String.valueOf(calculateCreationPrice())));
            buy.setDisabled(false);
        }
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.center;
    }

    @Override
    public boolean lockUpdate()
    {
        return true;
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE, getBatch());

        super.render();
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
