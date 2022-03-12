package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.settings.KeyProperties;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.common.msg.ItemActionMsg;
import com.desertkun.brainout.common.msg.client.FreePlayChangeSkinMsg;
import com.desertkun.brainout.common.msg.client.PutRecordIntoItemMsg;
import com.desertkun.brainout.common.msg.client.TakeRecordFromItemMsg;
import com.desertkun.brainout.components.ClientWeaponSlotComponent;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.WeaponSlotComponent;
import com.desertkun.brainout.components.my.MyPlayerComponent;
import com.desertkun.brainout.components.my.MyWeaponComponent;
import com.desertkun.brainout.content.Achievement;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.*;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.CustomConsumableItem;
import com.desertkun.brainout.content.consumable.impl.DefaultConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.consumable.impl.PlayerSkinConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.upgrades.ExtendedStorage;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ClientItemComponentData;
import com.desertkun.brainout.data.components.ClientMarketContainerComponentData;
import com.desertkun.brainout.data.components.FreeplayPlayerComponentData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.mode.ClientFreeRealization;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.ContentImage;
import com.desertkun.brainout.utils.MarketUtils;
import com.desertkun.brainout.utils.StringFunctions;
import com.esotericsoftware.minlog.Log;
import org.json.JSONObject;

public class MarketItemsCategoryMenu extends Menu implements EventReceiver
{
    private final Achievement marketPass;

    public static final Color WHITE_CLEAR = new Color(1, 1, 1, 0);
    private final String category;
    private final PlayerData playerData;

    protected MarketItemsInventoryPanel targetPanel;

    private InventoryPanel.InventoryDragAndDrop dragAndDrop;
    private Label weighInfo;
    private Label title;
    private TextButton friendly;
    private Table targetWeightRoot;
    private Table targetWeightUpgrades;
    private ProgressBar targetFill;
    private Label targetWeighInfo;
    private boolean marketUnlocked = false;
    private Button trashCan;

    public MarketItemsCategoryMenu(PlayerData playerData, String category)
    {
        this.playerData = playerData;
        this.category = category;

        dragAndDrop = new InventoryPanel.InventoryDragAndDrop()
        {
            @Override
            public void exchange(InventoryPanel.InventoryRecord record,
                                 InventoryPanel from, InventoryPanel to)
            {

                if (split(record.getRecord()))
                {
                    if (record.getRecord().getAmount() == 2)
                    {
                        move(record, 1, from, to);
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
                                move(record, amount, from, to);
                            }

                            @Override
                            public void cancel()
                            {

                            }
                        });
                    }
                }
                else
                {
                    move(record, record.getRecord().getAmount(), from, to);
                }

            }
        };

        marketPass = BrainOut.ContentMgr.get("market-pass", Achievement.class);
        if (marketPass != null)
        {
            marketUnlocked = BrainOutClient.ClientController.getUserProfile().hasItem(marketPass);
        }
    }

    private int getProfileRU()
    {
        return BrainOutClient.ClientController.getUserProfile().getInt("ru", 0);
    }

    private boolean split(ConsumableRecord record)
    {
        if (!record.getItem().splits())
        {
            return false;
        }

        if (ClientEnvironment.isMac())
        {
            return Gdx.input.isKeyPressed(Input.Keys.SYM);
        }

        return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
    }

    private ConsumableRecord getUniqueItem(String category, ConsumableContainer inventory)
    {
        // best is because of worst weapons have bigger 'weight'
        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : inventory.getData())
        {
            Content c = entry.value.getItem().getContent();

            UniqueComponent uc = c.getComponent(UniqueComponent.class);

            if (uc != null && uc.getCategory().equals(category))
            {
                return entry.value;
            }
        }

        return null;
    }

    private boolean takeItem(ConsumableRecord record, int amount, ActiveData from)
    {
        Content c = record.getItem().getContent();
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

        BrainOutClient.ClientController.sendTCP(new TakeRecordFromItemMsg(from.getId(), record, amount));

        return true;
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

    private boolean move(InventoryPanel.InventoryRecord record, int amount, InventoryPanel from, InventoryPanel to)
    {
        if (from instanceof ConsumableInventoryPanel)
        {
            if (to instanceof MarketItemsInventoryPanel)
            {
                return putItemIntoMarket(record.getRecord(), amount, (MarketItemsInventoryPanel) to);
            }
        }
        else if ((from instanceof FoundItemsInventoryPanel) && (to instanceof ConsumableInventoryPanel))
        {
            if (record instanceof FoundItemsInventoryPanel.FoundInventoryRecord)
            {
                FoundItemsInventoryPanel.FoundInventoryRecord found =
                    ((FoundItemsInventoryPanel.FoundInventoryRecord) record);

                if (!takeItem(found.getRecord(), amount, found.getItemData()))
                {
                    return false;
                }
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

        return true;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        data.add().colspan(3).height(20).row();

        weighInfo = new Label("", BrainOutClient.Skin, "title-small");
        targetWeighInfo = new Label("", BrainOutClient.Skin, "title-small");

        Image overWeight = new Image(BrainOutClient.Skin, "icon-overweight");
        overWeight.setVisible(false);


        // b
        {
            Table b = new Table();
            b.align(Align.top);

            {
                title = new Label(L.get("MENU_FOUND"), BrainOutClient.Skin, "title-small");
                title.setAlignment(Align.center);
                BorderActor ba = new BorderActor(title, 192, "button-tab-checked");
                b.add(ba).expandX().fillX().row();
            }

            targetPanel = createTargetPanel();

            targetPanel.getScrollPane().setScrollBarPositions(false, false);

            b.add(targetPanel).size(228, 392).row();

            targetWeightUpgrades = new Table(BrainOutClient.Skin);
            targetWeightRoot = new Table(BrainOutClient.Skin);
            targetWeightRoot.setBackground("form-gray");
            targetWeightRoot.align(Align.right);
            targetWeightRoot.add(targetWeighInfo).pad(1);
            targetWeightRoot.add(targetWeightUpgrades).row();

            b.add(targetWeightRoot).expandX().fillX().row();

            targetFill = new ProgressBar(
                0, 500, 0.05f, false,
                BrainOutClient.Skin, "progress-inventory");

            b.add(targetFill).expandX().fillX().padTop(-2).row();
            targetPanel.setHeight(targetPanel.getHeight() - targetWeightRoot.getHeight() - targetFill.getHeight());

            friendly = targetPanel.customFriendlyButton();

            if (friendly == null)
            {
                String key = Input.Keys.toString(
                    BrainOutClient.ClientSett.getControls().getKeyCode(KeyProperties.Keys.freePlayFriends, 0));

                friendly = new TextButton(L.get("MENU_FRIENDLY") + " [" + key + "]",
                    BrainOutClient.Skin, "button-checkable-green");
                friendly.setChecked(isFriendly());
                friendly.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        toggleFriendly();
                    }
                });
            }


            b.add(friendly).expandX().fillX().height(64).padTop(4).row();

            data.add(b).expandY().top().pad(10);
        }

        targetPanel.init();

        String title_ = targetPanel.getTitle();
        if (title_ != null)
        {
            title.setText(title_);
        }

        generateOtherPanels(data);

        return data;
    }

    protected void generateOtherPanels(Table data)
    {

    }

    protected MarketItemsInventoryPanel createTargetPanel()
    {
        ActiveData marketContainer = playerData.getMap().getClosestActiveForTag(10,
            playerData.getX(), playerData.getY(), ActiveData.class, Constants.ActiveTags.MARKET_CONTAINER,
            activeData -> activeData.getContent().hasComponent(ClientMarketContainerComponent.class));

        if (marketContainer != null)
        {
            return createMarketInventoryPanel(marketContainer);
        }

        return null;
    }

    private MarketItemsInventoryPanel createMarketInventoryPanel(ActiveData marketContainer)
    {
        return new MarketItemsInventoryPanel(marketContainer, dragAndDrop, category)
        {
            @Override
            protected boolean clicked(InventoryRecord record, boolean primary)
            {
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

                            popMeAndPushMenu(new MarketMenu(category));
                        }
                    });
                }
                else
                {
                    Tooltip.RegisterToolTip(market, new UnlockTooltip.UnlockTooltipCreator(
                        marketPass, BrainOutClient.ClientController.getUserProfile()
                    ), MarketItemsCategoryMenu.this);

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

    private void updateTargetWeight(float weight, float maxWeight)
    {
        if (targetWeightRoot != null)
        {
            targetFill.setValue(weight);
            targetFill.setRange(0, maxWeight);

            targetFill.setStyle(BrainOutClient.Skin.get(
                    weight > maxWeight * 0.95f ?
                            "progress-inventory-full" : (
                            weight > maxWeight * 0.75f ?
                                    "progress-inventory-half-full" : "progress-inventory")
                    , ProgressBar.ProgressBarStyle.class
            ));

            targetWeighInfo.setText(StringFunctions.format(
                    (float)(int)(weight * 100.0f) / 100.0f
            ) + " / " + StringFunctions.format(maxWeight) + " " + L.get("CHAR_SUFFIX_BLOCKS"));
        }

        if (targetWeightUpgrades != null)
        {
            targetWeightUpgrades.clear();

            UserProfile profile = BrainOutClient.ClientController.getUserProfile();
            ExtendedStorage roomToExtend = ExtendedStorage.HasRoomToExtend(profile);
            if (roomToExtend != null)
            {
                boolean locked = roomToExtend.isLocked(profile);

                Button upgrade = new TextButton("+", BrainOutClient.Skin, locked ? "button-disabled" : "button-tab");
                upgrade.setDisabled(locked);

                if (locked)
                {
                    upgrade.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.denied);
                        }
                    });

                    Tooltip.RegisterToolTip(upgrade,
                        new UnlockTooltip.UnlockTooltipCreator(roomToExtend, profile), MarketItemsCategoryMenu.this);
                }
                else
                {
                    Tooltip.RegisterToolTip(upgrade, roomToExtend.getTitle().get(), MarketItemsCategoryMenu.this);

                    upgrade.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            pushMenu(new ConfirmationPopup(L.get("MENU_EXTENDED_CAPACITY_UPGRADE",
                                String.valueOf(roomToExtend.getExtraWeight()),
                                String.valueOf(roomToExtend.getShopItem().getAmount())))
                            {
                                @Override
                                public void yes()
                                {
                                    JSONObject args = new JSONObject();
                                    args.put("id", roomToExtend.getID());

                                    BrainOutClient.SocialController.sendRequest("purchase_ownable", args,
                                        new SocialController.RequestCallback()
                                    {
                                        @Override
                                        public void success(JSONObject response)
                                        {
                                            Gdx.app.postRunnable(targetPanel::refresh);
                                        }

                                        @Override
                                        public void error(String reason)
                                        {
                                            Gdx.app.postRunnable(() ->
                                                MarketItemsCategoryMenu.this.pushMenu(new ConfirmationPopup(L.get(reason))
                                            {
                                                @Override
                                                public String buttonYes()
                                                {
                                                    return L.get("MENU_STORE");
                                                }

                                                @Override
                                                public String buttonNo()
                                                {
                                                    return L.get("MENU_CANCEL");
                                                }

                                                @Override
                                                public void yes()
                                                {
                                                    pushMenu(new StoreMenu());
                                                }

                                                @Override
                                                public String buttonStyleYes()
                                                {
                                                    return "button-green";
                                                }

                                                @Override
                                                protected float getButtonHeight()
                                                {
                                                    return 64;
                                                }

                                                @Override
                                                protected String getTitleBackgroundStyle()
                                                {
                                                    return "button-highlighted-normal";
                                                }

                                                @Override
                                                public void no()
                                                {
                                                    Menu.playSound(MenuSound.back);
                                                }
                                            }));
                                        }
                                    });
                                }

                                @Override
                                public void onInit()
                                {
                                    super.onInit();

                                    {
                                        int amount = BrainOutClient.ClientController.getUserProfile().getInt("ru", 0);

                                        Table ru = new Table(BrainOutClient.Skin);
                                        ru.setBackground("form-default");

                                        Table icon = new Table();
                                        ContentImage.RenderStatImage("ru", amount, icon);
                                        ru.add(icon).row();

                                        Label ruAvailableLabel = new Label(String.valueOf(amount), BrainOutClient.Skin, "title-small");
                                        ruAvailableLabel.setAlignment(Align.center);
                                        ru.add(ruAvailableLabel).expandX().fillX().row();

                                        ru.setBounds(BrainOutClient.getWidth() - 212, BrainOutClient.getHeight() - 84, 192, 64);

                                        addActor(ru);
                                    }
                                }

                                @Override
                                protected float getFade()
                                {
                                    return Constants.Menu.MENU_BACKGROUND_FADE_DOUBLE;
                                }

                                @Override
                                protected String getContentBackgroundStyle()
                                {
                                    return "form-default";
                                }

                                @Override
                                public String buttonYes()
                                {
                                    return L.get("MENU_MARKET_BUY");
                                }

                                @Override
                                public String buttonNo()
                                {
                                    return L.get("MENU_CANCEL");
                                }

                                @Override
                                public String buttonStyleYes()
                                {
                                    return "button-green";
                                }

                                @Override
                                protected String getTitleBackgroundStyle()
                                {
                                    return "button-highlighted-normal";
                                }

                                @Override
                                public void no()
                                {
                                    Menu.playSound(MenuSound.back);
                                }
                            });
                        }
                    });
                }

                targetWeightUpgrades.add(upgrade).size(24).padLeft(8);
            }
        }
    }

    private boolean itemShouldBeOnThirdPanel(ConsumableRecord record)
    {
        Content c = record.getItem().getContent();

        if (!(c instanceof Instrument))
        {
            return c.hasComponent(UniqueComponent.class);
        }

        Instrument instrument = (Instrument) record.getItem().getContent();

        if (instrument.getSlot() == null)
            return false;

        return instrument.getSlot().getID().equals("slot-primary") ||
                instrument.getSlot().getID().equals("slot-secondary");
    }

    private void highlight(InventoryPanel.ConsumableRecordPredicate predicate)
    {
        targetPanel.highlight(predicate);
    }

    private void highlightStatus(InventoryPanel.InventoryRecord iv, boolean h)
    {
        targetPanel.highlightStatus(iv, h);
    }

    private void hoverItem(InventoryPanel.InventoryRecord iv, boolean hovered)
    {
        ConsumableRecord record = iv.getRecord();
        highlightStatus(iv, hovered);

        if (hovered)
        {
            ConsumableItem item = record.getItem();

            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ic = ((InstrumentConsumableItem) item);

                if (ic.getInstrumentData() instanceof WeaponData)
                {
                    WeaponData weaponData = ((WeaponData) ic.getInstrumentData());

                    highlight(record12 ->
                    {
                        ConsumableItem item2 = record12.getItem();

                        if (item2 instanceof DefaultConsumableItem)
                        {
                            DefaultConsumableItem d = ((DefaultConsumableItem) item2);

                            if (d.getContent() instanceof Bullet)
                            {
                                Bullet b = ((Bullet) d.getContent());

                                MyWeaponComponent mwc = weaponData.getComponent(MyWeaponComponent.class);

                                if (mwc != null)
                                {
                                    for (ObjectMap.Entry<String, WeaponSlotComponent> entry : mwc.getSlots())
                                    {
                                        if (entry.value.getBullet().getID().equals(b.getID()))
                                            return true;
                                    }
                                }
                                else
                                {
                                    return weaponData.getWeapon().getPrimaryProperties().getBullet().equals(b.getID());
                                }
                            }
                        }

                        return false;
                    });
                }
            }
            else if (item instanceof DefaultConsumableItem)
            {
                DefaultConsumableItem d = ((DefaultConsumableItem) item);

                if (d.getContent() instanceof Bullet)
                {
                    Bullet b = ((Bullet) d.getContent());

                    highlight(record12 ->
                    {
                        ConsumableItem item2 = record12.getItem();

                        if (item2 instanceof InstrumentConsumableItem)
                        {
                            InstrumentConsumableItem ic = ((InstrumentConsumableItem) item2);

                            if (ic.getInstrumentData() instanceof WeaponData)
                            {
                                WeaponData weaponData = ((WeaponData) ic.getInstrumentData());

                                MyWeaponComponent mwc = weaponData.getComponent(MyWeaponComponent.class);

                                if (mwc != null)
                                {
                                    for (ObjectMap.Entry<String, WeaponSlotComponent> entry : mwc.getSlots())
                                    {
                                        if (entry.value.getBullet().getID().equals(b.getID()))
                                            return true;
                                    }
                                }
                                else
                                {
                                    return weaponData.getWeapon().getPrimaryProperties().getBullet().equals(b.getID());
                                }
                            }
                        }

                        return false;
                    });
                }
            }
        }
        else
        {
            highlight(record1 -> false);
        }
    }

    protected void addUserPanel()
    {
        UserPanel userPanel = BrainOutClient.Env.createUserPanel(false);
        userPanel.setBounds(20, getHeight() - 120, getWidth() - 40, 100);

        addActor(userPanel);
    }

    @Override
    public void onInit()
    {
        super.onInit();

        addUserPanel();

        Table events = new Table();
        PlayerSelectionMenu.RenderEvents(events, null, PlayerSelectionMenu.EventType.valuables,
            (event, t) -> pushMenu(new OnlineEventMenu(event, true)));

        events.setBounds(BrainOutClient.getWidth() - 412, BrainOutClient.getHeight() - 84, 192, 64);
        addActor(events);

        trashCan = new Button(BrainOutClient.Skin, "button-notext");
        trashCan.setBounds(BrainOutClient.getWidth() - 74, 10, 64, 64);
        Image trashIcon = new Image(BrainOutClient.Skin, "icon-trash");
        trashIcon.setScaling(Scaling.none);
        trashCan.add(trashIcon).expand().fill();

        trashCan.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);
                pushMenu(new AlertPopup(L.get("MENU_TRASHCAN_DESC")));
            }
        });

        addActor(trashCan);
        Tooltip.RegisterStandardToolTip(trashCan, L.get("MENU_TRASHCAN"), L.get("MENU_TRASHCAN_DESC"), this);

        dragAndDrop.addTarget(new DragAndDrop.Target(trashCan)
        {
            private boolean red()
            {
                trashCan.setStyle(BrainOutClient.Skin.get("button-red-drop", Button.ButtonStyle.class));
                return false;
            }

            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload,
                                float x, float y, int pointer)
            {
                if (!(source instanceof InventoryPanel.InventoryItem))
                {
                    return red();
                }

                trashCan.setStyle(BrainOutClient.Skin.get(
                        "button-highlighted-magazine", Button.ButtonStyle.class));

                return true;
            }

            @Override
            public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload)
            {
                trashCan.setStyle(BrainOutClient.Skin.get("button-inventory", Button.ButtonStyle.class));
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer)
            {
                destroyItems(source);
            }
        });

        BrainOutClient.EventMgr.subscribe(Event.ID.remoteClientUpdated, this);
        BrainOutClient.EventMgr.subscribeAt(Event.ID.gameController, this, true);
        BrainOutClient.EventMgr.subscribe(Event.ID.ammoLoaded, this);
    }

    private void destroyItems(DragAndDrop.Source source)
    {
        if (!(source instanceof InventoryPanel.InventoryItem))
        {
            return;
        }

        InventoryPanel.InventoryItem inventoryItem = ((InventoryPanel.InventoryItem) source);

        if ((inventoryItem.getRecord() instanceof MarketItemsInventoryPanel.MarketInventoryRecord))
        {
            if (!(targetPanel instanceof  MarketItemsInventoryPanel)) return;

            MarketItemsInventoryPanel market = ((MarketItemsInventoryPanel) targetPanel);

            MarketItemsInventoryPanel.MarketInventoryRecord record =
                (MarketItemsInventoryPanel.MarketInventoryRecord)inventoryItem.getRecord();

            JSONObject args = new JSONObject();
            args.put("market", market.getMarket());
            args.put("item", record.getMarketEntry().name);
            args.put("payload", record.getMarketEntry().payload);

            int amount = record.getRecord().getAmount();

            if (amount > 1)
            {
                pushMenu(new AmountMenu(amount, amount)
                {
                    @Override
                    public void approve(int splitAmount)
                    {
                        args.put("amount", splitAmount);

                        destroyItemRequest("destroy_market_item", args, market);
                    }

                    @Override
                    public void cancel()
                    {

                    }
                });
            }
            else
            {
                args.put("amount", amount);
                destroyItemRequest("destroy_market_item", args, market);
            }

        }

    }

    public void destroyItemRequest(String request, JSONObject args, InventoryPanel panel)
    {
        BrainOutClient.SocialController.sendRequest(request, args,
                new SocialController.RequestCallback()
                {
                    @Override
                    public void success(JSONObject response)
                    {
                        Menu.playSound(MenuSound.trash);
                        panel.refresh();
                    }

                    @Override
                    public void error(String reason)
                    {
                        Menu.playSound(MenuSound.denied);
                    }
                });
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        targetPanel.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.gameController, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.remoteClientUpdated, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.ammoLoaded, this);
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        KeyProperties.Keys action = BrainOutClient.ClientSett.getControls().getKey(keyCode);

        if (action != null)
        {
            switch (action)
            {
                case playerList:
                {
                    pop();

                    return true;
                }
                case freePlayFriends:
                {
                    toggleFriendly();

                    return true;
                }
            }
        }

        return super.keyDown(keyCode);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case gameController:
            {
                GameControllerEvent ev = ((GameControllerEvent) event);

                switch (ev.action)
                {
                    case activate:
                    {
                        pop();
                        return true;
                    }
                }

                break;
            }
            case remoteClientUpdated:
            {
                RemoteClientUpdatedEvent ev = ((RemoteClientUpdatedEvent) event);

                if (ev.remoteClient == BrainOutClient.ClientController.getMyRemoteClient())
                {
                    updateFriendly();
                }

                break;
            }
        }

        return false;
    }

    private void updateFriendly()
    {
        if (targetPanel.hasCustomFriendlyButton())
            return;

        friendly.setChecked(isFriendly());
    }

    private boolean isFriendly()
    {
        ClientRealization cr = (ClientRealization)BrainOutClient.ClientController.getGameMode().getRealization();

        if (!(cr instanceof ClientFreeRealization))
            return false;

        return ((ClientFreeRealization) cr).isFriendly();
    }

    private void toggleFriendly()
    {
        if (targetPanel.hasCustomFriendlyButton())
            return;

        ClientRealization cr = (ClientRealization)BrainOutClient.ClientController.getGameMode().getRealization();

        if (!(cr instanceof ClientFreeRealization))
            return;

        ((ClientFreeRealization) cr).toggleFriendly();
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        BrainOutClient.Env.getGameController().setControllerMode(GameController.ControllerMode.disabled);

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(0.5f, getBatch());

        super.render();
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

}
