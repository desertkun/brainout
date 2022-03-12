package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.content.Achievement;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.ClientMarketContainerComponent;
import com.desertkun.brainout.content.components.InventoryMoveSoundComponent;
import com.desertkun.brainout.content.components.UniqueComponent;
import com.desertkun.brainout.content.consumable.CustomConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.*;
import com.esotericsoftware.minlog.Log;
import org.json.JSONObject;

public class MarketExchangeInventoryMenu extends ExchangeInventoryMenu
{
    private final Achievement marketPass;
    private final ActiveData marketContainer;
    private boolean marketUnlocked = false;

    public MarketExchangeInventoryMenu(PlayerData playerData, ActiveData marketContainer)
    {
        super(playerData);
        this.marketContainer = marketContainer;

        marketPass = BrainOut.ContentMgr.get("market-pass", Achievement.class);
        if (marketPass != null)
        {
            marketUnlocked = BrainOutClient.ClientController.getUserProfile().hasItem(marketPass);
        }
    }

    public static ActiveData GetMarketContainer(PlayerData playerData)
    {
        return playerData.getMap().getClosestActiveForTag(10,
                playerData.getX(), playerData.getY(), ActiveData.class, Constants.ActiveTags.MARKET_CONTAINER,
                activeData -> activeData.getContent().hasComponent(ClientMarketContainerComponent.class));
    }

    @Override
    protected void addInventoryPanel(Table b)
    {
        b.add(targetPanel).size(228, 392).row();

        targetWeightUpgrades = new Table(BrainOutClient.Skin);
        targetWeightRoot = new Table(BrainOutClient.Skin);
        targetWeightRoot.setBackground("form-gray");
        targetWeightRoot.align(Align.right);
        targetWeightRoot.add(targetWeighInfo).pad(1);
        targetWeightRoot.add(targetWeightUpgrades).row();

        b.add(targetWeightRoot).expandX().fillX().row();

        targetFill = new ProgressBar(
                0, playerData.getMaxOverweight(), 0.05f, false,
                BrainOutClient.Skin, "progress-inventory");

        b.add(targetFill).expandX().fillX().padTop(-2).row();
        targetPanel.setHeight(targetPanel.getHeight() - targetWeightRoot.getHeight() - targetFill.getHeight());
    }

    private boolean putItemIntoMarket(ConsumableRecord record, int amount, MarketItemsInventoryPanel market)
    {
        int am = Math.min(market.canFitAnItem(record), amount);

        if (am == 0)
        {
            return false;
        }

        JSONObject args = new JSONObject();

        args.put("market", market.getMarket());
        args.put("id", record.getId());
        args.put("amount", am);

        BrainOutClient.SocialController.sendRequest("put_market_item", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                InventoryMoveSoundComponent snd = record.getItem().getContent().getComponent(InventoryMoveSoundComponent.class);

                if (snd != null)
                {
                    snd.play(playerData);
                }

                market.refresh();
            }

            @Override
            public void error(String reason)
            {
                if (Log.ERROR) Log.error(reason);
                Menu.playSound(MenuSound.denied);
            }
        });

        return true;
    }

    private boolean withdrawItemFromMarket(
            MarketItemsInventoryPanel.MarketInventoryRecord record, int amount, MarketItemsInventoryPanel market)
    {
        Content c = record.getRecord().getItem().getContent();
        if (c == null)
            return false;

        /*
        UniqueComponent uniqueComponent = c.getComponent(UniqueComponent.class);

        if (uniqueComponent != null && getUniqueItem(uniqueComponent.getCategory(), sourcePanel.getInventory()) != null)
        {
            Menu.playSound(MenuSound.denied);
            return false;
        }
         */

        JSONObject args = new JSONObject();

        args.put("market", market.getMarket());
        args.put("item", record.getMarketEntry().name);
        args.put("payload", record.getMarketEntry().payload);
        args.put("amount", amount);

        BrainOutClient.SocialController.sendRequest("withdraw_market_item", args,
                new SocialController.RequestCallback()
                {
                    @Override
                    public void success(JSONObject response)
                    {
                        InventoryMoveSoundComponent snd = c.getComponent(InventoryMoveSoundComponent.class);

                        if (snd != null)
                        {
                            snd.play(playerData);
                        }

                        market.refresh();
                    }

                    @Override
                    public void error(String reason)
                    {
                        Menu.playSound(MenuSound.denied);
                    }
                });

        return true;
    }

    @Override
    protected boolean move(InventoryPanel.InventoryRecord record, int amount, InventoryPanel from, InventoryPanel to)
    {
        if (from instanceof ConsumableInventoryPanel)
        {
            if (to instanceof MarketItemsInventoryPanel)
            {
                return putItemIntoMarket(record.getRecord(), amount, (MarketItemsInventoryPanel) to);
            }
        }
        else if ((from instanceof MarketItemsInventoryPanel) && (to instanceof ConsumableInventoryPanel))
        {
            if (record instanceof MarketItemsInventoryPanel.MarketInventoryRecord)
            {
                MarketItemsInventoryPanel.MarketInventoryRecord found =
                        ((MarketItemsInventoryPanel.MarketInventoryRecord) record);

                UniqueComponent u = found.getRecord().getItem().getContent().getComponent(UniqueComponent.class);

                if (amount == 1 || found.getRecord().getItem() instanceof InstrumentConsumableItem || u != null)
                {
                    return withdrawItemFromMarket(found, 1, (MarketItemsInventoryPanel) from);
                }
                else
                {
                    if (split(record.getRecord()))
                    {
                        if (record.getRecord().getAmount() == 2)
                        {
                            return withdrawItemFromMarket(found, 1, (MarketItemsInventoryPanel) from);
                        }
                        else
                        {
                            pushMenu(new AmountMenu(
                                    Math.max(record.getRecord().getAmount() / 2, 1),
                                    record.getRecord().getAmount()
                            ){
                                @Override
                                public void approve(int amount)
                                {
                                    withdrawItemFromMarket(found, amount, (MarketItemsInventoryPanel) from);
                                }

                                @Override
                                public void cancel()
                                {

                                }
                            });

                            return true;
                        }
                    }
                    else
                    {
                        return withdrawItemFromMarket(found, amount, (MarketItemsInventoryPanel) from);
                    }
                }
            }
        }

        return super.move(record, amount, from, to);
    }

    private InventoryPanel createMarketInventoryPanel(ActiveData marketContainer)
    {
        return new MarketItemsInventoryPanel(marketContainer, dragAndDrop, "default")
        {
            @Override
            protected boolean clicked(InventoryRecord record, boolean primary)
            {
                if (primary)
                {
                    if (split(record.getRecord()))
                    {
                        if (record.getRecord().getAmount() == 2)
                        {
                            return move(record, 1, targetPanel, sourcePanel);
                        }
                        else
                        {
                            pushMenu(new AmountMenu(
                                    Math.max(record.getRecord().getAmount() / 2, 1),
                                    record.getRecord().getAmount()
                            ){
                                @Override
                                public void approve(int amount)
                                {
                                    move(record, amount, targetPanel, sourcePanel);
                                }

                                @Override
                                public void cancel()
                                {

                                }
                            });

                            return true;
                        }
                    }
                    else
                    {
                        return move(record, record.getRecord().getAmount(), targetPanel, sourcePanel);
                    }
                }

                return true;
            }

            @Override
            protected void itemHovered(InventoryRecord record, boolean b)
            {
                hoverItem(record, b);
            }

            @Override
            protected void updated(float weight)
            {
                super.updated(weight);

                updateTargetWeight(weight, this.getMaxWeightForUser());
            }

            @Override
            public void highlightStatus(InventoryRecord iv, boolean h)
            {
                if (!(iv instanceof MarketInventoryRecord))
                    return;

                if (iv.getRecord().getItem() instanceof CustomConsumableItem)
                    return;

                if (!marketUnlocked)
                    return;

                friendly.setText(h ? L.get("MENU_NEW_SELL_ORDER") : L.get("MENU_MARKET"));
                friendly.setChecked(h);
            }

            @Override
            public boolean hasCustomFriendlyButton()
            {
                return true;
            }

            @Override
            public TextButton customFriendlyButton()
            {
                TextButton market = new TextButton(L.get("MENU_MARKET"),
                        BrainOutClient.Skin, "button-checkable-market");

                market.getLabel().setWrap(true);
                market.setDisabled(!marketUnlocked);

                if (marketUnlocked)
                {
                    dragAndDrop.addTarget(new DragAndDrop.Target(market)
                    {
                        @Override
                        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload,
                                            float x, float y, int pointer)
                        {
                            if (source instanceof InventoryItem)
                            {
                                InventoryItem inventoryItem = ((InventoryItem) source);

                                if (inventoryItem.getRecord() instanceof MarketInventoryRecord)
                                {
                                    if (!(inventoryItem.getRecord().getRecord().getItem() instanceof CustomConsumableItem))
                                    {
                                        market.setChecked(true);
                                        market.setText(L.get("MENU_NEW_SELL_ORDER"));
                                        return true;
                                    }
                                }
                            }

                            market.setDisabled(true);
                            return false;
                        }

                        @Override
                        public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload)
                        {
                            market.setText(L.get("MENU_MARKET"));
                            market.setChecked(false);
                            market.setDisabled(false);
                        }

                        @Override
                        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload,
                                         float x, float y, int pointer)
                        {
                            if (source instanceof InventoryItem)
                            {
                                InventoryItem inventoryItem = ((InventoryItem) source);

                                if (inventoryItem.getRecord() instanceof MarketInventoryRecord)
                                {
                                    MarketInventoryRecord marketInventoryRecord
                                            = ((MarketInventoryRecord) inventoryItem.getRecord());

                                    if (marketInventoryRecord.getRecord().getItem().getContent() instanceof Bullet)
                                    {
                                        Bullet bullet = (Bullet) marketInventoryRecord.getRecord().getItem().getContent();

                                        if (marketInventoryRecord.getMarketEntry().amount < bullet.getGood())
                                        {
                                            pushMenu(new AlertPopup(L.get("MENU_SELL_ORDER_MIN_QUANTITY",
                                                    String.valueOf(bullet.getGood()))));
                                            return;
                                        }
                                    }

                                    Menu.playSound(MenuSound.select);

                                    pushMenu(new NewSellOrderMenu(marketInventoryRecord,
                                            () -> ((MarketItemsInventoryPanel) targetPanel).refresh()));
                                }
                            }
                        }
                    });

                    market.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            popMeAndPushMenu(new MarketMenu("default"));
                        }
                    });
                }
                else
                {
                    Tooltip.RegisterToolTip(market, new UnlockTooltip.UnlockTooltipCreator(
                            marketPass, BrainOutClient.ClientController.getUserProfile()
                    ), MarketExchangeInventoryMenu.this);

                    market.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.denied);
                        }
                    });
                }

                return market;
            }
        };
    }

    @Override
    protected InventoryPanel createTargetPanel()
    {
        return createMarketInventoryPanel(marketContainer);
    }
}
