package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.ConsumableND;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.NotifyEvent;
import com.desertkun.brainout.menu.FormMenu;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.ClickOverListener;
import com.desertkun.brainout.menu.ui.LabeledSlider;
import com.desertkun.brainout.menu.ui.MarketItemsInventoryPanel;
import com.desertkun.brainout.utils.ContentImage;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.MarketService;
import org.json.JSONObject;

public class NewSellOrderMenu extends FormMenu
{
    private final MarketItemsInventoryPanel.MarketInventoryRecord marketInventoryRecord;
    private final ConsumableRecord record;
    private final Runnable sold;
    private TextButton create;
    private Label total;
    private int pieces;
    private int min;
    private int max;
    private int price;
    private int amount;

    private static int MarketFee = -1;
    private static int MaxPrice = -1;
    private static int MaxAptPrice = -1;
    private static int MarketFeeMinimum = 0;

    public NewSellOrderMenu(
        MarketItemsInventoryPanel.MarketInventoryRecord marketInventoryRecord, Runnable sold)
    {
        this.record = marketInventoryRecord.getRecord();
        this.marketInventoryRecord = marketInventoryRecord;

        if (marketInventoryRecord.getRecord().getItem().getContent() instanceof Bullet)
        {
            pieces = ((Bullet) marketInventoryRecord.getRecord().getItem().getContent()).getGood();
            min = pieces;
            max = (marketInventoryRecord.getMarketEntry().amount / pieces) * pieces;
        }
        else
        {
            pieces = 1;
            min = 1;
            max = marketInventoryRecord.getMarketEntry().amount;
        }

        price = 100;
        amount = max;
        this.sold = sold;
    }

    @Override
    public boolean escape()
    {
        pop();
        return true;
    }

    private boolean hasFeeSettings()
    {
        return MarketFee != -1;
    }

    private int getMarketFee()
    {
        return MarketFee;
    }

    private int getMarketFeeMinimum()
    {
        return MarketFeeMinimum;
    }

    private void getFeeSettings(Runnable done)
    {
        MarketService marketService = MarketService.Get();
        LoginService loginService = LoginService.Get();

        if (marketService == null || loginService == null)
            return;

        marketService.getMarketSettings("freeplay", loginService.getCurrentAccessToken(),
        (request, result, settings) -> {
            if (result == Request.Result.success)
            {
                MarketFee = settings.optInt("fee", 0);
                MarketFeeMinimum = settings.optInt("fee-minimum", 0);
                MaxPrice = settings.optInt("max-price", 50000);
                MaxAptPrice = settings.optInt("max-apt-price", MaxPrice);

                Gdx.app.postRunnable(done);
            }
            else
            {
                MarketFee = -1;
                Gdx.app.postRunnable(done);
            }
        });
    }

    @Override
    public Table createUI()
    {
        create = new TextButton(L.get("MENU_CREATE_FOR_RU", "..."), BrainOutClient.Skin, "button-green");

        Table body = new Table();

        body.add().padBottom(128).row();

        {
            Table header = new Table();
            header.setSkin(BrainOutClient.Skin);
            header.setBackground("form-red");

            {
                Label title = new Label(L.get("MENU_NEW_SELL_ORDER"), BrainOutClient.Skin, "title-yellow");
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

            if (hasFeeSettings())
            {
                renderPayload(payload);
            }
            else
            {
                getFeeSettings(() -> renderPayload(payload));
            }

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
                buttons.add(cancel).uniformX().expand().fill();
            }
            {
                create.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        if (!isPriceValid() || !isEnoughRUForAFee())
                        {
                            Menu.playSound(MenuSound.denied);
                            return;
                        }

                        createOrder();
                    }
                });
                buttons.add(create).uniformX().expand().fill();
            }

            body.add(buttons).expandX().fillX().height(64).row();
        }

        {
            Label disclaimer = new Label(L.get("MENU_MARKET_FEE_DISCLAIMER"), BrainOutClient.Skin, "title-gray");
            disclaimer.setWrap(true);
            body.add(disclaimer).padTop(128).padLeft(-128).padRight(-128).expandX().fillX().row();
        }

        return body;
    }

    private void createOrder()
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();

        args.put("market", "freeplay");
        args.put("item", marketInventoryRecord.getMarketEntry().name);
        args.put("payload", marketInventoryRecord.getMarketEntry().payload);
        args.put("price", price);
        args.put("amount", amount);

        BrainOutClient.SocialController.sendRequest("new_market_order", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                NewSellOrderMenu.this.pop();
                BrainOutClient.EventMgr.sendDelayedEvent(NotifyEvent.obtain(
                    NotifyAward.consumable, 1, NotifyReason.marketOrderPosted, NotifyMethod.message,
                    new ConsumableND(record)
                ));
                sold.run();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.popMeAndPushMenu(new AlertPopup(L.get(reason)));
            }
        });

    }

    private int calculateCreationPrice()
    {
        int p = price * (amount / pieces);
        int fee = getMarketFee() != 0 ? ((p * getMarketFee()) / 100) : 0;
        return Math.max(fee, getMarketFeeMinimum());
    }

    private void renderPayload(Table payload)
    {
        if (MarketFee == -1)
        {
            payload.add(new Label(L.get("MENU_ERROR_TRY_AGAIN"), BrainOutClient.Skin, "title-red"));
            return;
        }

        Label priceFor1 = new Label(
            pieces == 1 ? L.get("MENU_PRICE_FOR_1_PIECE") :
            L.get("MENU_PRICE_FOR_N_PIECES", String.valueOf(pieces)), BrainOutClient.Skin, "title-yellow");
        payload.add(priceFor1).pad(4).expandX().center().row();

        {
            Table item = new Table();

            {
                Table r1 = new Table(BrainOutClient.Skin);
                r1.setBackground("form-default");

                if (marketInventoryRecord.getRecord().getItem() instanceof InstrumentConsumableItem)
                {
                    InstrumentConsumableItem ici = ((InstrumentConsumableItem) marketInventoryRecord.getRecord().getItem());
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
                    ContentImage.RenderImage(marketInventoryRecord.getRecord().getItem().getContent(), r1, 1);
                }

                item.add(r1).size(192, 64);
            }
            {
                Table r2 = new Table(BrainOutClient.Skin);
                r2.setBackground("form-default");

                TextField num = new TextField(String.valueOf(price), BrainOutClient.Skin, "edit-empty");
                num.setAlignment(Align.right);
                Label RU = new Label("RU", BrainOutClient.Skin, "title-small");

                setKeyboardFocus(num);

                num.addListener(new ChangeListener()
                {
                    @Override
                    public void changed(ChangeEvent event, Actor actor)
                    {
                        int a;

                        try
                        {
                            a = Integer.valueOf(num.getText());
                        }
                        catch (NumberFormatException e)
                        {
                            a = 0;
                        }

                        price = a;

                        updateFee();
                        calculateTotal();
                        calculateCreationPrice();
                    }
                });

                r2.add(num).expand().padRight(4).fill();
                r2.add(RU).padLeft(4).padRight(8);

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
                    calculateCreationPrice();
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

        {
            Label fee = new Label(L.get("MENU_MARKET_FEE",
                String.valueOf(getMarketFee()),
                String.valueOf(getMarketFeeMinimum())), BrainOutClient.Skin, "title-red");
            payload.add(fee).expandX().center().pad(16).padTop(0).row();
        }

        calculateTotal();
        updateFee();
    }

    private void calculateTotal()
    {
        total.setText(String.valueOf(price * (amount / pieces)) + " RU");
    }

    private boolean isPriceValid()
    {
        int maxPrice = MaxPrice;

        if (marketInventoryRecord.getMarketEntry().name.equals("realestate"))
        {
            maxPrice = MaxAptPrice;
        }

        return MarketFee >= 0 && (price * (amount / pieces) <= maxPrice) && price > 0;
    }

    private boolean isEnoughRUForAFee()
    {
        return BrainOutClient.ClientController.getUserProfile().getStats().get("ru", 0f) >= calculateCreationPrice();
    }

    private void updateFee()
    {
        if (!isPriceValid())
        {
            create.setText(L.get("MENU_MARKET_INVALID_PRICE"));
            create.setDisabled(true);
            return;
        }

        create.setText(L.get("MENU_CREATE_FOR_RU", String.valueOf(calculateCreationPrice())));
        create.setDisabled(!isEnoughRUForAFee());
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
