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
import com.badlogic.gdx.utils.*;
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
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.mode.ClientFreeRealization;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.ContentImage;
import com.desertkun.brainout.utils.MarketUtils;
import com.desertkun.brainout.utils.StringFunctions;
import org.anthillplatform.runtime.services.MarketService;
import org.json.JSONObject;

public class ExchangeInventoryMenu extends Menu implements EventReceiver
{
    protected final PlayerData playerData;
    protected ActiveData placeInto;
    protected final ConsumableContainer inventory;

    public static final Color WHITE_CLEAR = new Color(1, 1, 1, 0);

    protected ConsumableInventoryPanel sourcePanel;
    protected ConsumableInventoryPanel weaponsPanel;
    protected InventoryPanel targetPanel;

    protected InventoryPanel.InventoryDragAndDrop dragAndDrop;
    protected Label weighInfo;
    protected Label title;
    protected TextButton friendly;
    protected Table targetWeightRoot;
    protected Table targetWeightUpgrades;
    protected ProgressBar targetFill;
    protected Label targetWeighInfo;
    protected Button trashCan;

    public ExchangeInventoryMenu(PlayerData playerData)
    {
        this.playerData = playerData;

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

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        if (poc != null)
        {
            this.inventory = poc.getConsumableContainer();
            this.inventory.updateWeight();
        }
        else
        {
            this.inventory = null;
        }
    }

    private int getProfileRU()
    {
        return BrainOutClient.ClientController.getUserProfile().getInt("ru", 0);
    }

    protected boolean split(ConsumableRecord record)
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

    private boolean putItem(ConsumableRecord record, int amount, ActiveData to)
    {
        BrainOutClient.ClientController.sendTCP(new PutRecordIntoItemMsg(to.getId(), record, amount));
        return true;
    }

    private boolean dropItem(ConsumableRecord record, int amount)
    {
        MyPlayerComponent myPlayerComponent = playerData.getComponent(MyPlayerComponent.class);

        if (myPlayerComponent == null)
            return false;

        return myPlayerComponent.dropItem(record, amount, 270);
    }

    protected boolean move(InventoryPanel.InventoryRecord record, int amount, InventoryPanel from, InventoryPanel to)
    {
        if (from instanceof ConsumableInventoryPanel)
        {
            if (this.placeInto != null)
            {
                if (placeInto.getCreator().hasComponent(MaxWeightComponent.class))
                {
                    MaxWeightComponent mx = placeInto.getCreator().getComponent(MaxWeightComponent.class);

                    if (mx != null)
                    {
                        ItemComponent itemComponent = record.getRecord().getItem().getContent().getComponent(ItemComponent.class);
                        ClientItemComponentData ici = placeInto.getComponent(ClientItemComponentData.class);
                        if (itemComponent != null && ici != null)
                        {
                            if (ici.getWeight() + itemComponent.getWeight() * amount > mx.getMaxWeight())
                            {
                                Menu.playSound(MenuSound.denied);
                                return false;
                            }
                        }
                    }
                }

                return putItem(record.getRecord(), amount, placeInto);
            }
            else
            {
                return dropItem(record.getRecord(), amount);
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

        return true;
    }

    private interface GetStat
    {
        float get();
    }

    private void renderStats(Table to)
    {
        FreeplayPlayerComponentData fp = playerData.getComponent(FreeplayPlayerComponentData.class);

        if (fp == null)
            return;

        to.clear();

        renderStat(to, "icon-boost-hunger-small", "progress-event", fp::getHunger, fp.getContentComponent().getHungerMax(), true);
        renderStat(to, "icon-boost-thirst-small", "progress-friend", fp::getThirst, fp.getContentComponent().getThirstMax(), true).row();
        renderStat(to, "icon-boost-cold-small", "progress-cold", fp::getTemperature, fp.getContentComponent().getTemperatureMax(), false);
        renderStat(to, "icon-boost-radx-small", "progress-radio", fp::getRadio, fp.getContentComponent().getRadioMax(), true).row();

    }

    private Cell<Table> renderStat(Table to, String icon, String progressBarStyle, GetStat value, float max, boolean numbers)
    {
        Table item = new Table(BrainOutClient.Skin);
        item.setBackground("form-default");

        Image img = new Image(BrainOutClient.Skin, icon);
        img.setScaling(Scaling.none);
        img.setOrigin(8, 8);
        item.add(img).size(16, 16).pad(4);

        ProgressBar progress = new ProgressBar(0, max, 0.01f, false, BrainOutClient.Skin, progressBarStyle);
        float v = value.get();
        progress.setValue(v);
        item.add(progress).expandX().fillX().height(8).pad(4);

        Label lbl;
        if (numbers)
        {
            lbl = new Label(String.valueOf((int)v), BrainOutClient.Skin, "title-small");
            lbl.setAlignment(Align.right);
            item.add(lbl).pad(2, 4, 4, 4).padLeft(-40).width(40);
        }
        else
        {
            lbl = null;
        }

        item.addAction(
            Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                Actions.delay(2.0f),
                Actions.run(() ->
                {
                    float v_ = value.get();
                    progress.setValue(v_);
                    if (numbers)
                    {
                        lbl.setText(String.valueOf((int) v_));
                    }
                })
            ))
        );

        return to.add(item).expandX().fillX();
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();

        data.add().colspan(3).height(20).row();

        ProgressBar fill = new ProgressBar(
            0, playerData.getMaxOverweight(), 0.05f, false,
            BrainOutClient.Skin, "progress-inventory");

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

            if (targetPanel == null)
            {
                targetPanel = createTargetPanel();
            }

            targetPanel.getScrollPane().setScrollBarPositions(false, false);

            addInventoryPanel(b);

            friendly = targetPanel.customFriendlyButton();

            if (friendly == null && !targetPanel.hasCustomFriendlyButton())
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

        // a
        {
            Table a = new Table();
            a.align(Align.top);

            Label title = new Label(L.get("MENU_INVENTORY"), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            BorderActor ba = new BorderActor(title, 192);
            ba.getCell().expandX().fillX();

            if (inventory != null && weighInfo != null)
            {
                updateWeight(inventory.getWeight(), playerData.getMaxOverweight(), playerData.getMaxWeight(),
                    fill, weighInfo, overWeight);
            }

            a.add(ba).left().row();

            sourcePanel = new ConsumableInventoryPanel(dragAndDrop, this.inventory,
                (record -> !itemShouldBeOnThirdPanel(record)))
            {
                @Override
                protected InventoryItem newInventoryItem(
                        InventoryRecord record, DragAndDropInventory inventory)
                {
                    InventoryItem item = super.newInventoryItem(record, inventory);

                    if (record.getRecord().getItem().getContent().getID().equals("instrument-guitar"))
                    {
                        ImageButton upgrade = new ImageButton(BrainOutClient.Skin, "button-upgrades");
                        upgrade.setBounds(0, 0, 24, 24);

                        upgrade.getListeners().insert(0, new ClickOverListener()
                        {
                            @Override
                            public void clicked(InputEvent event, float x, float y)
                            {
                                Menu.playSound(MenuSound.select);
                                event.stop();

                                ConsumableItem consumableItem = item.getRecord().getRecord().getItem();

                                if (!(consumableItem instanceof InstrumentConsumableItem))
                                    return;

                                pushMenu(new UpgradeInventoryInstrumentMenu(
                                        item.getRecord().getRecord(),
                                        ExchangeInventoryMenu.this.inventory
                                ));
                            }

                            @Override
                            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
                            {
                                event.stop();

                                return super.touchDown(event, x, y, pointer, button);
                            }

                            @Override
                            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                            {
                                super.enter(event, x, y, pointer, fromActor);
                                event.cancel();
                            }


                        });

                        item.getHolder().addActor(upgrade);
                    }

                    return item;
                }

                @Override
                protected DragAndDropInventory newDragAndDropInventory()
                {
                    return new DragAndDropInventory(pane, this::updateBackgroundPanel)
                    {
                        @Override
                        protected boolean extraRecordCheck(InventoryRecord record)
                        {
                            return record.withdrawable();
                        }
                    };
                }

                @Override
                protected boolean clicked(InventoryRecord record, boolean primary)
                {
                    if (record == null)
                    {
                        return false;
                    }

                    if (primary)
                    {
                        if (split(record.getRecord()))
                        {
                            if (record.getRecord().getAmount() == 2)
                            {
                                return move(record, 1, sourcePanel, targetPanel);
                            }
                            else
                            {
                                pushMenu(new AmountMenu(
                                        Math.max(record.getRecord().getAmount() / 2, 1),
                                        record.getRecord().getAmount()){
                                    @Override
                                    public void approve(int amount)
                                    {
                                        move(record, amount, sourcePanel, targetPanel);
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
                            return move(record, record.getRecord().getAmount(), sourcePanel, targetPanel);
                        }
                    }
                    else
                    {
                        activate(record);
                    }

                    return true;
                }

                @Override
                protected void itemHovered(InventoryRecord record, boolean b)
                {
                    hoverItem(record, b);
                }

                @Override
                protected void updated()
                {
                    updateWeight(inventory.getWeight(), playerData.getMaxOverweight(), playerData.getMaxWeight(),
                            fill, weighInfo, overWeight);
                }
            };

            a.add(sourcePanel).size(428, 390).row();

            {
                Table weightRoot = new Table(BrainOutClient.Skin);
                weightRoot.setBackground("form-gray");

                Label totalWeight = new Label(L.get("MENU_TOTAL_WEIGHT") + ":", BrainOutClient.Skin, "title-yellow");
                weightRoot.add(totalWeight).padLeft(8).expandX().left();
                weightRoot.add(overWeight).size(16);
                weightRoot.add(weighInfo).pad(1).row();

                a.add(weightRoot).expandX().fillX().row();
                a.add(fill).expandX().fillX().padTop(-2).row();
            }

            sourcePanel.getScrollPane().setScrollBarPositions(true, false);

            if (needStats())
            {
                Table stats = new Table();
                renderStats(stats);
                a.add(stats).expandX().fillX().row();
            }

            data.add(a).expand().left().top().pad(10).fill();
        }

        // c
        {
            Table c = new Table();
            c.align(Align.top);

            weaponsPanel = new ConsumableInventoryPanel(dragAndDrop, this.inventory, this::itemShouldBeOnThirdPanel)
            {
                @Override
                protected boolean clicked(InventoryRecord record, boolean primary)
                {
                    if (primary)
                    {
                        return move(record, record.getRecord().getAmount(), weaponsPanel, targetPanel);
                    }
                    else
                    {
                        activate(record);
                    }

                    return true;
                }

                @Override
                protected DragAndDropInventory newDragAndDropInventory()
                {
                    return new WeaponDragAndDropInventory(pane, this::updateBackgroundPanel);
                }

                @Override
                protected void initBackground()
                {
                    Image bg = new Image(BrainOutClient.Skin, "bg-player");
                    bg.setScaling(Scaling.none);
                    bg.setAlign(Align.left);
                    bg.setFillParent(true);
                    bg.setTouchable(Touchable.disabled);

                    addActor(bg);
                }

                @Override
                protected boolean noWrap()
                {
                    return true;
                }

                @Override
                protected InventoryItem newInventoryItem(
                    InventoryRecord record, DragAndDropInventory inventory)
                {
                    InventoryItem item = new InventoryItem(record, inventory)
                    {
                        @Override
                        public Actor extendedSection(Actor old)
                        {
                            ConsumableRecord r = record.getRecord();
                            ConsumableItem it = r.getItem();

                            if (!(it.getContent() instanceof Weapon))
                            {
                                return null;
                            }

                            if (!(it instanceof InstrumentConsumableItem))
                            {
                                return null;
                            }

                            InstrumentConsumableItem ici = ((InstrumentConsumableItem) it);
                            InstrumentData instrumentData = ici.getInstrumentData();

                            Weapon weapon = ((Weapon) record.getRecord().getItem().getContent());

                            if (!weapon.getPrimaryProperties().hasMagazineManagement())
                            {
                                return null;
                            }

                            PlayerData me = BrainOutClient.ClientController.getState(CSGame.class).getPlayerData();
                            if (me == null)
                                return null;


                            MyWeaponComponent mwc = instrumentData.getComponent(MyWeaponComponent.class);

                            if (mwc == null)
                            {
                                mwc = new MyWeaponComponent(((WeaponData) instrumentData), r);
                                instrumentData.addComponent(mwc);
                                mwc.init();
                            }

                            WeaponSlotComponent mainSlot = mwc.getSlot(Constants.Properties.SLOT_PRIMARY);
                            if (!(mainSlot instanceof ClientWeaponSlotComponent))
                            {
                                return null;
                            }

                            Group section;
                            if (old != null)
                            {
                                section = ((Group) old);
                                section.clear();
                            }
                            else
                            {
                                section = new Group();
                            }

                            int i = 0;

                            if (mainSlot.hasMagazineAttached())
                            {
                                TextureRegion region = BrainOutClient.Skin.getRegion(
                                    ClientConstants.Menu.FreePlay.GetMagazineImage(
                                        (float)mainSlot.getRounds() / mainSlot.getClipSize().asFloat()));

                                Button upgrade = new Button(BrainOutClient.Skin, "button-selected-magazine");

                                Image magazine = new Image(new TextureRegionDrawable(region));
                                magazine.setTouchable(Touchable.disabled);
                                magazine.setFillParent(true);
                                magazine.setAlign(Align.center);
                                magazine.setScaling(Scaling.none);
                                upgrade.addActor(magazine);

                                final Label bulletsCount = new Label(String.valueOf(mainSlot.getRounds()),
                                    BrainOutClient.Skin, "title-yellow");

                                bulletsCount.setAlignment(Align.center, Align.center);
                                bulletsCount.setFillParent(true);
                                bulletsCount.setTouchable(Touchable.disabled);
                                bulletsCount.setVisible(false);
                                upgrade.addActor(bulletsCount);

                                upgrade.addListener(new ClickOverListener()
                                {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
                                    {
                                        playSound(MenuSound.denied);
                                        return super.touchDown(event, x, y, pointer, button);
                                    }

                                    @Override
                                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                                    {
                                        super.enter(event, x, y, pointer, fromActor);

                                        bulletsCount.setVisible(true);
                                        magazine.getColor().a = 0.3f;
                                    }

                                    @Override
                                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                                    {
                                        super.exit(event, x, y, pointer, toActor);

                                        bulletsCount.setVisible(false);
                                        magazine.getColor().a = 1;
                                    }
                                });

                                upgrade.setBounds(0, 0, 48, 64);
                                section.addActor(upgrade);

                                dragAndDrop.addTarget(new DragAndDrop.Target(upgrade)
                                {
                                    @Override
                                    public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload,
                                                        float x, float y, int pointer)
                                    {
                                        upgrade.setStyle(BrainOutClient.Skin.get("button-red-drop", Button.ButtonStyle.class));

                                        return true;
                                    }

                                    @Override
                                    public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload)
                                    {
                                        upgrade.setStyle(BrainOutClient.Skin.get("button-selected-magazine", Button.ButtonStyle.class));
                                    }

                                    @Override
                                    public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload,
                                                     float x, float y, int pointer)
                                    {
                                    }
                                });

                                resetTarget();

                                i++;
                            }

                            IntMap.Keys keys = mainSlot.getMagazines();

                            while (keys.hasNext)
                            {
                                int magazineId = keys.next();
                                int status = mainSlot.getMagazineStatus(magazineId);

                                Button upgrade = new Button(BrainOutClient.Skin, "button-inventory");
                                upgrade.setBounds(i * 48, 0, 48, 64);

                                TextureRegion region = BrainOutClient.Skin.getRegion(
                                    ClientConstants.Menu.FreePlay.GetMagazineImage(
                                        (float)status / mainSlot.getClipSize().asFloat()
                                    )
                                );

                                Image magazine = new Image(new TextureRegionDrawable(region));
                                magazine.setTouchable(Touchable.disabled);
                                magazine.setFillParent(true);
                                magazine.setAlign(Align.center);
                                magazine.setScaling(Scaling.none);
                                upgrade.addActor(magazine);

                                final Label bulletsCount = new Label(String.valueOf(status), BrainOutClient.Skin, "title-yellow");
                                bulletsCount.setAlignment(Align.center, Align.center);
                                bulletsCount.setFillParent(true);
                                bulletsCount.setTouchable(Touchable.disabled);
                                bulletsCount.setVisible(false);
                                upgrade.addActor(bulletsCount);

                                upgrade.addListener(new ClickOverListener()
                                {
                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
                                    {
                                        if (button == Input.Buttons.RIGHT)
                                        {
                                            BrainOut.EventMgr.sendEvent(SelectSlotEvent.obtain(0, Constants.Properties.SLOT_PRIMARY));
                                            loadMagazine(magazineId, me, mainSlot, r);
                                        }
                                        else
                                        {
                                            unloadMagazine(magazineId, me, mainSlot, r);
                                        }

                                        return super.touchDown(event, x, y, pointer, button);
                                    }

                                    @Override
                                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                                    {
                                        super.enter(event, x, y, pointer, fromActor);

                                        bulletsCount.setVisible(true);
                                        magazine.getColor().a = 0.3f;
                                    }

                                    @Override
                                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                                    {
                                        super.exit(event, x, y, pointer, toActor);

                                        bulletsCount.setVisible(false);
                                        magazine.getColor().a = 1;
                                    }
                                });

                                section.addActor(upgrade);

                                dragAndDrop.addTarget(new DragAndDrop.Target(upgrade)
                                {
                                    private boolean red()
                                    {
                                        upgrade.setStyle(BrainOutClient.Skin.get("button-red-drop", Button.ButtonStyle.class));
                                        return false;
                                    }

                                    @Override
                                    public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload,
                                                        float x, float y, int pointer)
                                    {
                                        if (!(source instanceof InventoryItem))
                                        {
                                            return red();
                                        }

                                        InventoryItem item = ((InventoryItem) source);

                                        if (!item.getRecord().withdrawable())
                                        {
                                            return red();
                                        }

                                        if (!(item.getRecord().getRecord().getItem().getContent() instanceof Bullet))
                                        {
                                            return red();
                                        }

                                        Bullet bullet = ((Bullet) item.getRecord().getRecord().getItem().getContent());
                                        if (mainSlot.getBullet() != bullet)
                                        {
                                            return red();
                                        }

                                        upgrade.setStyle(BrainOutClient.Skin.get("button-highlighted-magazine", Button.ButtonStyle.class));

                                        return true;
                                    }

                                    @Override
                                    public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload)
                                    {
                                        upgrade.setStyle(BrainOutClient.Skin.get("button-inventory", Button.ButtonStyle.class));
                                    }

                                    @Override
                                    public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload,
                                                     float x, float y, int pointer)
                                    {
                                        if (!(source instanceof InventoryItem))
                                        {
                                            return;
                                        }

                                        InventoryItem item = ((InventoryItem) source);

                                        if (item.getRecord().getRecord().getItem().getContent() instanceof Bullet)
                                        {
                                            Bullet bullet = ((Bullet) item.getRecord().getRecord().getItem().getContent());
                                            if (mainSlot.getBullet() == bullet)
                                            {
                                                BrainOut.EventMgr.sendEvent(SelectSlotEvent.obtain(0, Constants.Properties.SLOT_PRIMARY));
                                                loadMagazine(magazineId, me, mainSlot, r);
                                                return;
                                            }
                                        }
                                    }
                                });

                                resetTarget();

                                i++;
                            }

                            return section;
                        }

                        @Override
                        public boolean extendedSectionToTheRight()
                        {
                            return BrainOutClient.getWidth() >= 1200;
                        }

                        @Override
                        public boolean extraInfoOnWeapons()
                        {
                            return true;
                        }

                        @Override
                        public boolean forceWrap()
                        {
                            return true;
                        }

                        public void unloadMagazine(int magazineId, PlayerData me, WeaponSlotComponent mainSlot, ConsumableRecord r)
                        {
                            PlayerOwnerComponent poc = me.getComponent(PlayerOwnerComponent.class);
                            if (poc == null)
                            {
                                return;
                            }

                            if (poc.getCurrentInstrument() != null && poc.getCurrentInstrument().isForceSelect())
                            {
                                Menu.playSound(MenuSound.denied);
                                return;
                            }

                            int chargedAmount = mainSlot.getMagazineStatus(magazineId);

                            if (chargedAmount <= 0)
                                playSound(MenuSound.denied);
                            else
                                mainSlot.doUnloadAllRounds(magazineId);
                        }

                        public void loadMagazine(int magazineId, PlayerData me, WeaponSlotComponent mainSlot, ConsumableRecord r)
                        {
                            PlayerOwnerComponent poc = me.getComponent(PlayerOwnerComponent.class);
                            if (poc == null || !poc.isEnabled())
                            {
                                return;
                            }

                            int notChargedAmount = poc.getConsumableContainer().getAmount(mainSlot.getBullet());
                            int clipSize = mainSlot.getClipSize().asInt();
                            int chargedAmount = mainSlot.getMagazineStatus(magazineId);

                            if (poc.getCurrentInstrument() != null && poc.getCurrentInstrument().isForceSelect())
                            {
                                Menu.playSound(MenuSound.denied);
                                return;
                            }

                            poc.setCurrentInstrument(r);

                            BrainOut.EventMgr.sendDelayedEvent(
                                    SimpleEvent.obtain(SimpleEvent.Action.instrumentUpdated));

                            if (notChargedAmount <= 0 || chargedAmount >= clipSize)
                            {
                                playSound(MenuSound.denied);
                            }
                            else
                            {
                                ExchangeInventoryMenu.this.pop();
                                mainSlot.doLoadRounds(magazineId);
                            }
                        }
                    };

                    if (record.getRecord().getItem().getContent() instanceof Weapon)
                    {
                        ImageButton upgrade = new ImageButton(BrainOutClient.Skin, "button-upgrades");
                        upgrade.setBounds(0, 0, 24, 24);

                        upgrade.getListeners().insert(0, new ClickOverListener()
                        {
                            @Override
                            public void clicked(InputEvent event, float x, float y)
                            {
                                Menu.playSound(MenuSound.select);
                                event.stop();

                                ConsumableItem consumableItem = item.getRecord().getRecord().getItem();

                                if (!(consumableItem instanceof InstrumentConsumableItem))
                                    return;

                                pushMenu(new UpgradeInventoryInstrumentMenu(
                                        item.getRecord().getRecord(),
                                    ExchangeInventoryMenu.this.inventory
                                ));
                            }

                            @Override
                            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button)
                            {
                                event.stop();

                                return super.touchDown(event, x, y, pointer, button);
                            }

                            @Override
                            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                            {
                                super.enter(event, x, y, pointer, fromActor);
                                event.cancel();
                            }


                        });

                        item.getHolder().addActor(upgrade);
                    }

                    return item;
                }

                /*
                @Override
                protected void initForeground()
                {
                    ImageButton img = new ImageButton(BrainOutClient.Skin, "button-upgrades");
                    img.setBounds(0, 0, 16, 16);
                    addActor(img);
                }
                */

                @Override
                protected void updateBackgroundPanel(UpdateBackgroundPanelStatus status)
                {
                    if (status == UpdateBackgroundPanelStatus.good)
                    {
                        setBackground("form-empty-good");
                    }
                    else
                    {
                        setBackground("form-empty");
                    }
                }

                @Override
                protected String getItemButtonStyle()
                {
                    return "button-inventory";
                }

                @Override
                protected void itemHovered(InventoryRecord record, boolean b)
                {
                    hoverItem(record, b);
                }
            };

            float w = BrainOutClient.getWidth() >= 1200 ? 384 : 192;
            float r = BrainOutClient.getWidth() >= 1200 ? -64 : 0;
            c.add(weaponsPanel).padTop(34).size(w + 16, 498).row();
            data.add(c).padLeft(16).padRight(r).width(w).top().row();
        }

        sourcePanel.init();
        targetPanel.init();
        weaponsPanel.init();

        placeInto = targetPanel.getPlaceInto();
        float weight = 0;
        float maxWeight = 0;

        if (targetPanel instanceof MarketItemsInventoryPanel)
        {
            MarketItemsInventoryPanel marketPanel = (MarketItemsInventoryPanel) targetPanel;

            weight = marketPanel.getCurrentWeight();

            ClientMarketContainerComponentData marketContainer = marketPanel.getMarketContainer();
            if (marketContainer != null)
            {
                placeInto = (ActiveData) marketContainer.getComponentObject();

                if (placeInto != null)
                {
                    Active containerContent = ((ActiveData) marketContainer.getComponentObject()).getCreator();
                    UserProfile profile = BrainOutClient.ClientController.getUserProfile();
                    maxWeight = MarketUtils.GetMaxMarketWeightForPlayer(containerContent, profile, "default");
                }
            }
        }

        else if (targetPanel.getPlaceInto() != null)
        {
            placeInto = targetPanel.getPlaceInto();

            ClientItemComponentData cic = placeInto.getComponent(ClientItemComponentData.class);
            MaxWeightComponent mx = placeInto.getCreator().getComponent(MaxWeightComponent.class);

            if (mx != null && cic != null)
            {
                cic.updateWeight();

                weight = cic.getWeight();
                maxWeight = mx.getMaxWeight();
            }

            if (placeInto != null && maxWeight > 0)
            {
                updateTargetWeight(weight, maxWeight);

                BrainOutClient.ClientController.sendTCP(new ItemActionMsg(placeInto, "open"));
            }
        }

        String title_ = targetPanel.getTitle();
        if (title_ != null)
        {
            title.setText(title_);
        }

        return data;
    }

    protected boolean needStats()
    {
        return true;
    }

    protected void addInventoryPanel(Table b)
    {
        b.add(targetPanel).size(228, 422).row();
    }

    protected InventoryPanel createTargetPanel()
    {
        return createFoundItemsInventoryPanel();
    }

    private InventoryPanel createFoundItemsInventoryPanel()
    {
        return new FoundItemsInventoryPanel(dragAndDrop, this.playerData)
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
            public void updated(ActiveData item)
            {
                super.updated(item);

                if (placeInto != null)
                {
                    ClientItemComponentData cic = placeInto.getComponent(ClientItemComponentData.class);
                    MaxWeightComponent mx = placeInto.getCreator().getComponent(MaxWeightComponent.class);

                    if (mx != null && cic != null)
                    {
                        cic.updateWeight();
                        updateTargetWeight(cic.getWeight(), mx.getMaxWeight());
                    }
                }
            }

            @Override
            protected void itemHovered(InventoryRecord record, boolean b)
            {
                hoverItem(record, b);
            }
        };
    }

    protected boolean allowWeightUpgrade()
    {
        return true;
    }

    protected void updateTargetWeight(float weight, float maxWeight)
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
            ) + " / " + StringFunctions.format(maxWeight) + " " + L.get("CHAR_SUFFIX_KG"));
        }

        if (targetWeightUpgrades != null && allowWeightUpgrade())
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
                        new UnlockTooltip.UnlockTooltipCreator(roomToExtend, profile), ExchangeInventoryMenu.this);
                }
                else
                {
                    Tooltip.RegisterToolTip(upgrade, roomToExtend.getTitle().get(), ExchangeInventoryMenu.this);

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
                                                ExchangeInventoryMenu.this.pushMenu(new ConfirmationPopup(L.get(reason))
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

    private void activate(InventoryPanel.InventoryRecord record)
    {
        if (playerData == null)
            return;

        SimplePhysicsComponentData spc = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);
        if (spc != null && !spc.hasAnyContact() && !spc.hasFixture())
            return;

        if (playerData.getCurrentInstrument() != null)
        {
            MyWeaponComponent mwc = playerData.getCurrentInstrument().getComponent(MyWeaponComponent.class);
            if (mwc != null)
            {

                WeaponSlotComponent currentSlot = mwc.getCurrentSlot();
                if (currentSlot instanceof ClientWeaponSlotComponent)
                {

                    WeaponSlotComponent.State state = currentSlot.getState();
                    if (
                            state == WeaponSlotComponent.State.loadMagazineRound ||
                                    state == WeaponSlotComponent.State.reloading ||
                                    state == WeaponSlotComponent.State.fetching
                    )
                    {

                        return;
                    }
                }
            }
        }

        ConsumableRecord consumableRecord = record.getRecord();
        ConsumableItem item = consumableRecord.getItem();

        if (item instanceof PlayerSkinConsumableItem)
        {
            PlayerSkinConsumableItem ps = ((PlayerSkinConsumableItem) item);

            changeSkin(consumableRecord);
            pop();

            return;
        }

        ClientItemActivatorComponent activator = item.getContent().getComponentFrom(ClientItemActivatorComponent.class);

        if (activator != null)
        {
            activator.activate(consumableRecord);
            pop();
            return;
        }

        MyPlayerComponent myPlayerComponent = playerData.getComponent(MyPlayerComponent.class);

        if (myPlayerComponent == null)
            return;

        myPlayerComponent.selectRecord(consumableRecord, Constants.Properties.SLOT_PRIMARY);

        pop();
    }

    private void changeSkin(ConsumableRecord record)
    {
        BrainOutClient.ClientController.sendTCP(new FreePlayChangeSkinMsg(record));
    }

    private static void updateWeight(float weight, float maxOverWeight, float maxWeight,
                              ProgressBar progressBar, Label weighInfo, Image overWeight)
    {
        progressBar.setValue(weight);
        progressBar.setRange(0, maxOverWeight);

        float progress = weight / maxOverWeight;

        progressBar.setStyle(BrainOutClient.Skin.get(
            weight > maxOverWeight ?
            "progress-inventory-full" : (
                weight > maxWeight ?
                "progress-inventory-half-full" : "progress-inventory")
            , ProgressBar.ProgressBarStyle.class
        ));

        weighInfo.setText(StringFunctions.format(
            (float)(int)(weight * 100.0f) / 100.0f
        ) + " / " + StringFunctions.format(maxOverWeight) + " " + L.get("CHAR_SUFFIX_KG"));

        if (overWeight != null)
        {
            overWeight.clearActions();

            if (progress > 0.8f)
            {
                overWeight.setVisible(true);

                overWeight.addAction(Actions.repeat(
                        RepeatAction.FOREVER,
                        Actions.sequence(
                                Actions.delay(0.5f),
                                Actions.color(WHITE_CLEAR, 0.25f),
                                Actions.delay(0.5f),
                                Actions.color(Color.WHITE, 0.25f)
                        )
                ));
            }
            else
            {
                overWeight.setVisible(false);
            }
        }
    }

    private void highlight(InventoryPanel.ConsumableRecordPredicate predicate)
    {
        weaponsPanel.highlight(predicate);
        sourcePanel.highlight(predicate);
        targetPanel.highlight(predicate);
    }

    private void highlightStatus(InventoryPanel.InventoryRecord iv, boolean h)
    {
        weaponsPanel.highlightStatus(iv, h);
        sourcePanel.highlightStatus(iv, h);
        targetPanel.highlightStatus(iv, h);
    }

    protected void hoverItem(InventoryPanel.InventoryRecord iv, boolean hovered)
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

    protected boolean allowQuestsButton()
    {
        return true;
    }

    @Override
    public void onInit()
    {
        super.onInit();

        UserPanel userPanel = BrainOutClient.Env.createUserPanel(false);
        userPanel.setBounds(20, getHeight() - 120, getWidth() - 40, 100);

        addActor(userPanel);

        if (allowQuestsButton())
        {
            MenuHelper.AddSingleButton(this, L.get("MENU_QUESTS"), () ->
            {
                Menu.playSound(MenuSound.select);

                popMeAndPushMenu(new ImGameFreePlayQuestsMenu(playerData));
            });
        }
        else
        {
            MenuHelper.AddCloseButton(this, this::pop);
        }

        Table events = new Table();
        PlayerSelectionMenu.RenderEvents(events, null, PlayerSelectionMenu.EventType.valuables,
            (event, t) -> pushMenu(new OnlineEventMenu(event, true)));

        events.setBounds(BrainOutClient.getWidth() - 412, BrainOutClient.getHeight() - 84, 192, 64);
        addActor(events);

        if (playerData.getMap().isSafeMap())
        {
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
        }

        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
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
        else
        {
            JSONObject args = new JSONObject();
            ConsumableRecord record = inventoryItem.getRecord().getRecord();

            args.put("id", record.getId());
            int amount = record.getAmount();

            if (amount > 1)
            {
                pushMenu(new AmountMenu(amount, amount)
                {
                    @Override
                    public void approve(int splitAmount)
                    {
                        args.put("amount", splitAmount);

                        destroyItemRequest("destroy_inventory_item", args, sourcePanel);
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
                destroyItemRequest("destroy_inventory_item", args, sourcePanel);
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

        weaponsPanel.release();
        sourcePanel.release();
        targetPanel.release();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.gameController, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.remoteClientUpdated, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.ammoLoaded, this);

        if (placeInto != null)
        {
            BrainOutClient.ClientController.sendTCP(new ItemActionMsg(placeInto, "close"));
        }
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
            case simple:
            {
                SimpleEvent simpleEvent = ((SimpleEvent) event);

                switch (simpleEvent.getAction())
                {
                    case consumablesUpdated:
                    {
                        sourcePanel.refresh();
                        weaponsPanel.refresh();

                        break;
                    }
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
