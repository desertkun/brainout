package com.desertkun.brainout.menu.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.components.WeaponSlotComponent;
import com.desertkun.brainout.components.my.MyWeaponComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.active.Item;
import com.desertkun.brainout.content.active.RealEstateItem;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.components.*;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.RealEstateContent;
import com.desertkun.brainout.content.consumable.Resource;
import com.desertkun.brainout.content.consumable.impl.*;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.PlayerSkinSlotItem;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.content.upgrades.UpgradeChain;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.impl.MarketMenu;
import com.desertkun.brainout.mode.ClientFreeRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.utils.ContentImage;

public abstract class InventoryPanel extends Table
{
    protected final ScrollPane pane;
    protected float currentWeight;

    public static class InventoryRecord
    {
        private final ConsumableRecord record;

        public InventoryRecord(ConsumableRecord record)
        {
            this.record = record;
        }

        public boolean withdrawable() { return true; }

        public ConsumableRecord getRecord()
        {
            return record;
        }
    }


    private final WidgetGroup contents;
    private final ObjectMap<Button, Group> extendedWidgets;
    protected final InventoryDragAndDrop dragAndDrop;
    private final DragAndDropInventory inventoryDragAndDrop;
    private final ClickOverListener clickOverListener;

    private OrderedMap<ConsumableRecord, InventoryItem> records;
    private OrderedMap<ConsumableRecord, InventoryRecord> preInitRecords;

    private class Placeholder
    {
        public final String name;
        public final String tagFilter;
        public final Item.ItemFilter filter;
        public final Array<Actor> actors = new Array<>();
        public final Array<Actor> required = new Array<>();

        public Placeholder(String name, Item.ItemFilter filter)
        {
            this.name = name;
            this.filter = filter;
            this.tagFilter = null;
        }
        public Placeholder(String name, String tagFilter)
        {
            this.name = name;
            this.filter = null;
            this.tagFilter = tagFilter;
        }
    }

    private ObjectMap<String, Placeholder> placeholders;

    public abstract static class InventoryDragAndDrop extends DragAndDrop
    {
        public InventoryDragAndDrop()
        {
            setDragTime(0);
        }

        public abstract void exchange(InventoryRecord record, InventoryPanel from, InventoryPanel to);
    }

    public static enum UpdateBackgroundPanelStatus
    {
        good,
        bad,
        def
    }

    public interface DragCallback
    {
        void updateStatus(UpdateBackgroundPanelStatus status);
    }

    private Placeholder findPlaceholder(Content content)
    {
        for (ObjectMap.Entry<String, Placeholder> entry : placeholders)
        {
            if (entry.value.filter != null)
            {
                if (entry.value.filter.matches(content))
                    return entry.value;
            }
            if (entry.value.tagFilter != null)
            {
                ItemComponent itemComponent = content.getComponent(ItemComponent.class);
                if (itemComponent != null && itemComponent.getTags(content) != null &&
                        itemComponent.getTags(content).contains(entry.value.tagFilter, false))
                {
                    return entry.value;
                }
            }
        }

        return null;
    }

    public static class ConsumableItemTooltip implements Tooltip.TooltipCreator
    {
        private final ConsumableRecord record;
        private final boolean useless;

        public ConsumableItemTooltip(ConsumableRecord record, boolean useless)
        {
            this.record = record;
            this.useless = useless;
        }

        public static boolean Need(ConsumableRecord record)
        {
            ItemComponent item = record.getItem().getContent().getComponent(ItemComponent.class);

            return record.hasQuality() ||
                record.getItem().getContent().getDescription().isValid() ||
                (item != null && (item.getWeight() * record.getAmount() > 0.2f)) ||
                ConsumableItemCharacteristics.HasAnyCharacteristic(record);
         }

        @Override
        public Actor get()
        {
            Table content = new Tooltip.TooltipTable();

            int charsHeight = 0;

            {
                Table header = new Table(BrainOutClient.Skin);
                header.setBackground("form-dark-blue");

                Label title = new Label(record.getItem().getContent().getTitle().get(),
                        BrainOutClient.Skin, "title-yellow");
                title.setAlignment(Align.center);
                header.add(title).center().row();

                content.add(header).expandX().fillX().row();
            }

            {
                Table payload = new Table(BrainOutClient.Skin);
                payload.setBackground("border-dark-blue");

                if (record.getItem().getContent().getDescription().isValid())
                {
                    Label title = new Label(record.getItem().getContent().getDescription().get(),
                        BrainOutClient.Skin, "title-small");
                    title.setAlignment(Align.center);
                    title.setWrap(true);
                    title.setAlignment(Align.left);
                    payload.add(title).pad(8).padBottom(8).expandX().fillX().left().row();
                    charsHeight += 32;
                }

                if (useless)
                {
                    Label title = new Label(L.get("MENU_USELESS_ITEM_DESC"),
                        BrainOutClient.Skin, "title-small");
                    title.setAlignment(Align.center);
                    title.setWrap(true);
                    title.setAlignment(Align.left);
                    payload.add(title).expandX().fillX().left().row();
                    charsHeight += 32;
                }

                {
                    Table chars_ = new Table();
                    ConsumableItemCharacteristics chars = newConsumableItemCharacteristics(useless, record);
                    chars.initChars();
                    chars_.add(chars).expand().fill().row();
                    payload.add(chars_).pad(8).padTop(8).expand().fill().row();

                    charsHeight += chars.getExtraHeight();
                }

                if (charsHeight > 0)
                {
                    content.add(payload).expand().fill().row();
                }

                ItemComponent item = record.getItem().getContent().getComponent(ItemComponent.class);

                if (item != null || record.hasQuality())
                {
                    Table bottomRow = new Table(BrainOutClient.Skin);
                    bottomRow.setBackground("border-dark-blue");
                    content.add(bottomRow).expandX().fillX().row();

                    if (record.hasQuality())
                    {
                        Label condition = new Label(L.get("CHAR_DURABILITY"), BrainOutClient.Skin, "title-small");
                        bottomRow.add(condition).padLeft(8);

                        Label c = new Label(String.valueOf(record.getQuality()) + "%", BrainOutClient.Skin, "title-small");

                        c.setColor(
                            MathUtils.lerp(1.0f, 0.f, (float)record.getQuality() / 100.0f),
                            MathUtils.lerp(0.0f, 1.f, (float)record.getQuality() / 100.0f),
                            0.f, 1.0f
                        );

                        bottomRow.add(c).expandX().left().padLeft(8);
                    }

                    if (item != null)
                    {
                        float w = item.getWeight() * record.getAmount();

                        if (item.isSpace())
                        {
                            Label condition = new Label(L.get("MENU_GARAGE_SPACE"), BrainOutClient.Skin, "title-small");
                            bottomRow.add(condition).expandX().left().padLeft(8);

                            Label weight = new Label(
                                "" + (int)w + " " + L.get("CHAR_SUFFIX_BLOCKS"),
                                BrainOutClient.Skin, "title-yellow");

                            bottomRow.add(weight).expandX().right().padRight(8).row();
                        }
                        else
                        {
                            Label weight = new Label(
                                String.format("%.2f", w) + " " + L.get("CHAR_SUFFIX_KG"),
                                BrainOutClient.Skin, "title-yellow");

                            bottomRow.add(weight).expandX().colspan(2).right().padRight(8).row();
                        }

                    }

                    RealEstateItemContainerComponent rsitemcnt =
                        record.getItem().getContent().getComponent(RealEstateItemContainerComponent.class);

                    if (rsitemcnt != null)
                    {
                        {
                            Label condition = new Label(L.get("MENU_STORAGE_PROVIDED"), BrainOutClient.Skin, "title-small");
                            bottomRow.add(condition).expandX().left().padLeft(8);

                            Label weight = new Label(
                                String.valueOf(rsitemcnt.getWeightLimit()) + " " + L.get("CHAR_SUFFIX_KG"),
                                BrainOutClient.Skin, "title-small");

                            bottomRow.add(weight).expandX().right().padRight(8).row();
                        }

                        if (rsitemcnt.getTagLimit() != null)
                        {
                            Label condition = new Label(L.get("MENU_SUITABLE_ONLY_FOR"), BrainOutClient.Skin, "title-small");
                            bottomRow.add(condition).expandX().left().padLeft(8);

                            Label weight = new Label(
                                L.get("MENU_SUITABLE_AS_" + rsitemcnt.getTagLimit().toUpperCase()),
                                BrainOutClient.Skin, "title-small");

                            bottomRow.add(weight).expandX().right().padRight(8).row();

                        }
                    }
                }
            }

            content.setSize(480, 32 + charsHeight);

            return content;
        }

        protected ConsumableItemCharacteristics newConsumableItemCharacteristics(boolean useless, ConsumableRecord record)
        {
            return new ConsumableItemCharacteristics(useless, record);
        }
    }

    public static class RecipeItemTooltip extends ConsumableItemTooltip
    {
        private final ObjectMap<Resource, Integer> resources;

        public RecipeItemTooltip(ConsumableRecord record, boolean useless, ObjectMap<Resource, Integer> resources)
        {
            super(record, useless);

            this.resources = resources;
        }

        @Override
        protected ConsumableItemCharacteristics newConsumableItemCharacteristics(boolean useless, ConsumableRecord record)
        {
            return new RecipeCharacteristics(useless, record, resources);
        }
    }


    public static class InstrumentTooltip implements Tooltip.TooltipCreator
    {
        private final InstrumentInfo info;
        private final int quality;

        public InstrumentTooltip(InstrumentInfo info, int quality)
        {
            this.info = info;
            this.quality = quality;
        }

        @Override
        public Actor get()
        {
            Table content = new Tooltip.TooltipTable();

            {
                Table header = new Table(BrainOutClient.Skin);
                header.setBackground("form-dark-blue");

                Label title = new Label(info.skin.getTitle().get(),
                    BrainOutClient.Skin, "title-yellow");
                title.setAlignment(Align.center);
                header.add(title).center().row();

                content.add(header).expandX().fillX().row();
            }

            {
                Table payload = new Table(BrainOutClient.Skin);
                payload.setBackground("border-dark-blue");

                InstrumentCharacteristics chars = new InstrumentCharacteristics(info, quality);
                payload.add(chars).expand().fillX().pad(4).padLeft(16).padRight(16).row();

                Table upgrades = new Table(BrainOutClient.Skin);

                for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
                {
                    Upgrade u = entry.value;

                    if (u instanceof UpgradeChain.ChainedUpgrade)
                        continue;

                    IconComponent iconComponent = u.getComponent(IconComponent.class);

                    Table holder = new Table(BrainOutClient.Skin);
                    //holder.setBackground("form-border-red");

                    TextureRegion tx = iconComponent.getIcon("big-icon");

                    if (tx == null)
                        continue;

                    Image icon = new Image(tx);
                    icon.setScaling(Scaling.fit);
                    holder.add(icon).height(32).minWidth(32).maxWidth(45).padBottom(10);

                    upgrades.add(holder).spaceRight(16);
                }

                VerticalGroup chainedUpgrades = new VerticalGroup();
                chainedUpgrades.wrap();

                for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
                {
                    Upgrade u = entry.value;

                    if (!(u instanceof UpgradeChain.ChainedUpgrade))
                        continue;

                    UpgradeChain.ChainedUpgrade ch = ((UpgradeChain.ChainedUpgrade) u);

                    IconComponent iconComponent = u.getComponent(IconComponent.class);

                    Table holder = new Table(BrainOutClient.Skin);
                    //holder.setBackground("form-border-red");

                    TextureRegion tx = iconComponent.getIcon("big-icon");

                    if (tx == null)
                        continue;

                    //float c = MathUtils.clamp(ch.getLevel() * 0.2f, 0.2f, 1.0f);

                    Image icon = new Image(tx);
                    //icon.setColor(1, 1, 1, c);
                    icon.setScaling(Scaling.none);
                    holder.padRight(15);
                    holder.add(icon).size(32, 32);

                    Label value = new Label(String.valueOf(ch.getLevel()),
                            BrainOutClient.Skin, "title-small");
                    value.setAlignment(Align.center);
                    //value.setColor(1, 1, 1, c);
                    holder.add(value).height(52);

                    chainedUpgrades.addActor(holder);
                }

                upgrades.add(chainedUpgrades).padLeft(1).height(64).maxHeight(64).row();

                payload.add(upgrades).padTop(4).padBottom(8).row();

                content.add(payload).expand().fill().row();
            }

            content.setSize(544, 340);

            return content;
        }
    }

    public class DragAndDropInventory extends DragAndDrop.Target
    {
        protected final DragCallback callback;

        public DragAndDropInventory(Actor actor, DragCallback callback)
        {
            super(actor);

            this.callback = callback;
        }

        public InventoryPanel getPanel()
        {
            return InventoryPanel.this;
        }

        protected boolean extraRecordCheck(InventoryRecord record)
        {
            return true;
        }

        @Override
        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer)
        {
            if (!(source instanceof InventoryItem))
                return false;

            InventoryItem item = ((InventoryItem) source);

            if (item.getInventory() == this)
                return false;

            if (!extraRecordCheck(item.getRecord()))
            {
                callback.updateStatus(UpdateBackgroundPanelStatus.bad);
                return false;
            }

            callback.updateStatus(UpdateBackgroundPanelStatus.good);

            return true;
        }

        @Override
        public void reset(DragAndDrop.Source source, DragAndDrop.Payload payload)
        {
            callback.updateStatus(UpdateBackgroundPanelStatus.def);
        }

        @Override
        public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer)
        {
            if (!(source instanceof InventoryItem))
                return;

            InventoryItem item = ((InventoryItem) source);

            if (item.getInventory() == this)
                return;


            dragAndDrop.exchange(item.getRecord(),
                    item.getInventory().getPanel(), InventoryPanel.this);

        }
    }

    public class WeaponDragAndDropInventory extends DragAndDropInventory
    {
        public WeaponDragAndDropInventory(Actor actor, InventoryPanel.DragCallback callback)
        {
            super(actor, callback);
        }

        @Override
        protected boolean extraRecordCheck(InventoryRecord record)
        {
            return record.withdrawable();
        }

        @Override
        public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer)
        {
            if (!(source instanceof InventoryItem))
                return false;

            InventoryItem item = ((InventoryItem) source);

            if (item.getInventory() == this)
                return false;

            if (item.record.getRecord().getItem().getContent() instanceof Bullet)
            {
                callback.updateStatus(UpdateBackgroundPanelStatus.bad);
                return false;
            }

            callback.updateStatus(UpdateBackgroundPanelStatus.good);
            return true;
        }
    }

    protected boolean noWrap()
    {
        return false;
    }

    public class InventoryItem extends DragAndDrop.Source
    {
        private final InventoryRecord record;
        private final DragAndDropInventory inventory;

        public InventoryItem(InventoryRecord record, DragAndDropInventory inventory)
        {
            super(new Button(BrainOutClient.Skin, getItemButtonStyle())
            {
                @Override
                public float getPrefWidth()
                {
                    return 192;
                }

                @Override
                public float getPrefHeight()
                {
                    return 64;
                }
            });

            this.record = record;
            this.inventory = inventory;

            renderContentsItem(getHolder(), true);

            getHolder().setSize(192, 64);

            getActor().setUserObject(this);
            getActor().addListener(clickOverListener);
        }

        public InventoryRecord getRecord()
        {
            return record;
        }

        public DragAndDropInventory getInventory()
        {
            return inventory;
        }

        public Actor extendedSection(Actor old)
        {
            return null;
        }

        public boolean extraInfoOnWeapons()
        {
            return false;
        }

        public boolean forceWrap()
        {
            return false;
        }

        public boolean extendedSectionToTheRight()
        {
            return false;
        }

        public float extendedSectionWidth()
        {
            return 192;
        }

        protected void renderContentsItem(Table holder, boolean numbers)
        {
            ConsumableItem item = record.getRecord().getItem();

            boolean useless = false, questRelated = false;

            GameMode gameMode = BrainOutClient.ClientController.getGameMode();

            if (gameMode != null && gameMode.getRealization() instanceof ClientFreeRealization)
            {
                ClientFreeRealization free = ((ClientFreeRealization) gameMode.getRealization());

                useless = free.isItemUseless(item);
                questRelated = free.isItemQuestRelated(item);
            }

            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ic = ((InstrumentConsumableItem) item);

                if (item.getContent().hasComponent(IconComponent.class))
                {
                    ContentImage.RenderImage(item.getContent(), holder, record.getRecord().getAmount());
                }
                else
                {
                    Skin skin = ic.getInstrumentData().getInfo().skin;
                    if (skin != null && skin.hasComponent(IconComponent.class) &&
                        skin.getComponent(IconComponent.class).hasIcon("big-icon"))
                    {
                        ContentImage.RenderImage(skin, holder, record.getRecord().getAmount());
                    }
                    else
                    {
                        boolean unloaded = false;
                        int rounds = -1;

                        if (extraInfoOnWeapons())
                        {
                            MyWeaponComponent mwp = ic.getInstrumentData().getComponent(MyWeaponComponent.class);
                            if (mwp != null)
                            {
                                WeaponSlotComponent slot = mwp.getSlot(Constants.Properties.SLOT_PRIMARY);
                                if (slot != null)
                                {
                                    if (slot.isDetached())
                                    {
                                        unloaded = true;
                                    }
                                }
                            }
                            else
                            {
                                if (ic.getInstrumentData() instanceof WeaponData)
                                {
                                    WeaponData weaponData = ((WeaponData) ic.getInstrumentData());
                                    WeaponData.WeaponLoad load = weaponData.getLoad(Constants.Properties.SLOT_PRIMARY);
                                    if (load != null)
                                    {
                                        if (load.isDetached())
                                        {
                                            unloaded = true;
                                        }
                                    }
                                }
                            }
                        }

                        ContentImage.RenderInstrument(holder, ic.getInstrumentData().getInfo(), unloaded);
                    }
                }

                if (ic.getInstrumentData() instanceof WeaponData)
                {
                    Gdx.app.postRunnable(() ->
                        Tooltip.RegisterToolTip(getActor(),
                            new InstrumentTooltip(ic.getInstrumentData().getInfo(), record.record.getQuality()),
                            getStage()));
                }
                else
                {
                    if (useless)
                    {
                        Gdx.app.postRunnable(() ->
                            Tooltip.RegisterStandardToolTip(getActor(),
                                item.getContent().getTitle().get() + " (" + L.get("MENU_USELESS_ITEM") + ")",
                                L.get("MENU_USELESS_ITEM_DESC"), getStage()));
                    }
                    else
                    {
                        if (record.getRecord().hasQuality())
                        {
                            Gdx.app.postRunnable(() ->
                                Tooltip.RegisterToolTip(getActor(),
                                    item.getContent().getTitle().get() +
                                        " (" + L.get("MENU_CONDITION") + record.getRecord().getQuality() + "%)", getStage()));
                        }
                        else
                        {
                            Gdx.app.postRunnable(() ->
                                Tooltip.RegisterToolTip(getActor(),
                                    item.getContent().getTitle().get(), getStage()));
                        }
                    }
                }
            }
            else if (item instanceof RealEstateConsumableItem)
            {
                RealEstateConsumableItem rs = ((RealEstateConsumableItem) item);
                RealEstateContent content = rs.getContent();

                ContentImage.RenderImage(content, holder, record.getRecord().getAmount());

                Gdx.app.postRunnable(() ->
                    Tooltip.RegisterToolTip(holder,
                        new InventoryPanel.ConsumableItemTooltip(record.getRecord(), false), getStage()));
            }
            else if (item instanceof RealEstateItemConsumableItem)
            {
                RealEstateItemConsumableItem rs = ((RealEstateItemConsumableItem) item);
                RealEstateItem content = rs.getContent();

                ContentImage.RenderImage(content, holder, record.getRecord().getAmount());

                Gdx.app.postRunnable(() ->
                    Tooltip.RegisterToolTip(holder,
                        new InventoryPanel.ConsumableItemTooltip(record.getRecord(), false), getStage()));
            }
            else if (item instanceof FlashDriveConsumableItem)
            {
                FlashDriveConsumableItem fl = ((FlashDriveConsumableItem) item);
                ConsumableContent content = fl.getContent();

                ContentImage.RenderImage(content, holder, record.getRecord().getAmount());

                Gdx.app.postRunnable(() ->
                        Tooltip.RegisterStandardToolTip(getActor(), content.getTitle().get(),
                                L.get("MENU_CODE", fl.getCode()), getStage()));
            }
            else if (item instanceof PlayerSkinConsumableItem)
            {
                PlayerSkinConsumableItem sk = ((PlayerSkinConsumableItem) item);
                PlayerSkin skin = sk.getContent();
                PlayerSkinSlotItem slotItem = skin.getSlotItem();

                ContentImage.RenderImage(slotItem, holder, record.getRecord().getAmount());

                if (slotItem.getDescription().isValid())
                {
                    Gdx.app.postRunnable(() ->
                        Tooltip.RegisterStandardToolTip(getActor(), slotItem.getTitle().get(),
                            slotItem.getDescription().get(), getStage()));
                }
                else
                {
                    Gdx.app.postRunnable(() ->
                        Tooltip.RegisterToolTip(getActor(), slotItem.getTitle().get(), getStage()));
                }
            }
            else
            {
                Content content = item.getContent();

                InstrumentPartialIconComponent pt = content.getComponent(InstrumentPartialIconComponent.class);

                if (pt != null)
                {
                    pt.renderImage(holder);
                }
                else
                {
                    ContentImage.RenderImage(content, holder, record.getRecord().getAmount());
                }

                if (item.getContent().hasComponent(FreePlayValuableComponent.class))
                {
                    FreePlayValuableComponent v = content.getComponent(FreePlayValuableComponent.class);

                    Gdx.app.postRunnable(() ->
                        Tooltip.RegisterStandardToolTip(getActor(), content.getTitle().get(),
                            L.get("MENU_VALUABLE", String.valueOf(v.getValue())), getStage()));
                }
                else
                {
                    boolean finalUseless = useless;

                    if (useless || InventoryPanel.ConsumableItemTooltip.Need(record.getRecord()))
                    {
                        Gdx.app.postRunnable(() ->
                            Tooltip.RegisterToolTip(getActor(),
                                new InventoryPanel.ConsumableItemTooltip(record.getRecord(), finalUseless), getStage()));
                    }
                    else
                    {
                        Gdx.app.postRunnable(() ->
                            Tooltip.RegisterToolTip(holder,
                                record.getRecord().getItem().getContent().getTitle().get(), getStage()));
                    }

                }
            }

            if (!numbers)
            {
                return;
            }

            ContentImage.RenderUsesAndAmount(record.getRecord(), holder);

            float y = 42;

            if (questRelated)
            {
                Image star = new Image(BrainOutClient.Skin, "quest-icon-star");
                star.setBounds(4, y, 19, 18); y -= 20;
                star.setTouchable(Touchable.disabled);
                holder.addActor(star);
            }
            if (useless)
            {
                Image star = new Image(BrainOutClient.Skin, "useless-item");
                star.setBounds(4, y, 19, 18);
                star.setTouchable(Touchable.disabled);
                holder.addActor(star);
            }
        }

        public Button getHolder()
        {
            return ((Button) getActor());
        }

        @Override
        public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target)
        {
            getActor().addAction(Actions.sequence(
                Actions.delay(0.25f),
                Actions.visible(true)
            ));
        }

        @Override
        public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer)
        {
            getActor().setVisible(false);

            DragAndDrop.Payload payload = new DragAndDrop.Payload();

            Group offset = new Group();

            Table holder = new Table(BrainOutClient.Skin);
            renderContentsItem(holder, false);
            offset.addActor(holder);

            payload.setDragActor(offset);
            payload.setObject(getRecord());

            return payload;
        }

        public void update()
        {
            Group old = extendedWidgets.get(getHolder());
            if (old != null)
            {
                extendedSection(old);
            }

            getHolder().clearChildren();
            renderContentsItem(getHolder(), true);
            getActor().setUserObject(this);
            getActor().addListener(clickOverListener);
        }
    }

    protected String getItemButtonStyle()
    {
        return "button-notext";
    }

    public InventoryPanel(InventoryDragAndDrop dragAndDrop)
    {
        super(BrainOutClient.Skin);

        this.dragAndDrop = dragAndDrop;
        this.records = new OrderedMap<>();
        this.preInitRecords = new OrderedMap<>();
        this.placeholders = new ObjectMap<>();
        this.extendedWidgets = new ObjectMap<>();

        this.clickOverListener = new ClickOverListener(-1)
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                if (event.isStopped())
                    return;

                Actor target = event.getListenerActor();
                if (target.getUserObject() instanceof InventoryItem)
                {
                    InventoryItem inventoryItem = ((InventoryItem) target.getUserObject());
                    if (InventoryPanel.this.clicked(inventoryItem.getRecord(), event.getButton() == 0))
                    {
                        Menu.playSound(Menu.MenuSound.select);
                    }
                    else
                    {
                        Menu.playSound(Menu.MenuSound.denied);
                    }
                }
                else
                {
                    Menu.playSound(Menu.MenuSound.select);
                }
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
            {
                if (event.isStopped())
                    return;

                super.enter(event, x, y, pointer, fromActor);

                Actor target = event.getListenerActor();
                if (target.getUserObject() instanceof InventoryItem)
                {
                    InventoryItem inventoryItem = ((InventoryItem) target.getUserObject());
                    InventoryPanel.this.itemHovered(inventoryItem.getRecord(), true);
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
            {
                if (event.isStopped())
                    return;

                super.exit(event, x, y, pointer, toActor);

                Actor target = event.getListenerActor();
                if (target.getUserObject() instanceof InventoryItem)
                {
                    InventoryItem inventoryItem = ((InventoryItem) target.getUserObject());
                    InventoryPanel.this.itemHovered(inventoryItem.getRecord(), false);
                }
            }
        };

        initBackground();

        if (noWrap())
        {
            VerticalGroup wh = new VerticalGroup();
            wh.align(Align.left | Align.top);
            wh.wrap(false);
            wh.space(8);
            wh.columnAlign(Align.left);
            contents = wh;
        }
        else
        {
            HorizontalGroup hh = new HorizontalGroup();
            hh.align(Align.left | Align.top);
            hh.wrap(true);
            hh.wrapSpace(8);
            hh.space(8);
            hh.rowAlign(Align.left);
            contents = hh;
        }

        pane = new ScrollPane(contents, BrainOutClient.Skin, "scroll-no-background");
        add(pane).pad(4).expand().fill().row();

        pane.addListener(new InputListener()
        {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
            {
                getStage().setScrollFocus(pane);
            }
        });

        pane.setScrollingDisabled(true, false);
        pane.setFadeScrollBars(false);

        inventoryDragAndDrop = newDragAndDropInventory();

        dragAndDrop.addTarget(inventoryDragAndDrop);
    }

    public void resetTarget()
    {
        dragAndDrop.removeTarget(inventoryDragAndDrop);
        dragAndDrop.addTarget(inventoryDragAndDrop);
    }

    protected DragAndDropInventory newDragAndDropInventory()
    {
        return new DragAndDropInventory(pane, this::updateBackgroundPanel);
    }

    protected void updateBackgroundPanel(UpdateBackgroundPanelStatus status)
    {
        switch (status)
        {
            case good:
            {
                setBackground("form-drag-good");
                break;
            }
            case bad:
            {
                setBackground("form-border-red");
                break;
            }
            case def:
            {
                setBackground("form-default");
                break;
            }
        }
    }

    protected void initBackground()
    {
        setBackground("form-default");
    }

    public void clearItems()
    {
        records.clear();
        contents.clear();
    }

    protected int getItemRank(ConsumableRecord record)
    {
        ConsumableItem item = record.getItem();

        SortingRankComponent rank = item.getContent().getComponent(SortingRankComponent.class);

        if (rank != null)
        {
            return rank.getRank();
        }

        if (item instanceof InstrumentConsumableItem)
        {
            if (item.getContent() instanceof Weapon)
            {
                Weapon weapon = ((Weapon) item.getContent());

                if (weapon.getSlot() != null)
                {
                    if (weapon.getSlot().getID().equals("slot-primary"))
                    {
                        return 30;
                    }
                }
            }

            return 20;
        }

        return 10;
    }

    private int getItemRank(InventoryRecord record)
    {
        return getItemRank(record.getRecord());
    }

    public ScrollPane getScrollPane()
    {
        return pane;
    }

    protected boolean clicked(InventoryRecord record, boolean primary)
    {
        return false;
    }

    protected void itemHovered(InventoryRecord record, boolean hovered)
    {

    }

    protected void updated()
    {
        //
    }

    protected void addPlaceholder(Actor placeholder, String kind, Item.ItemFilter filter)
    {
        contents.addActor(placeholder);

        Placeholder ph = placeholders.get(kind);
        if (ph == null)
        {
            ph = new Placeholder(kind, filter);
            placeholders.put(kind, ph);
        }

        ph.actors.add(placeholder);
    }

    protected void addPlaceholder(Actor placeholder, String kind, String tagFilter)
    {
        contents.addActor(placeholder);

        Placeholder ph = placeholders.get(kind);
        if (ph == null)
        {
            ph = new Placeholder(kind, tagFilter);
            placeholders.put(kind, ph);
        }

        ph.actors.add(placeholder);
    }

    protected void addItem(InventoryRecord record)
    {
        ConsumableItem consumableItem = record.getRecord().getItem();

        if (consumableItem.isPrivate() && consumableItem.getPrivate() != BrainOutClient.ClientController.getMyId())
        {
            return;
        }

        Content c = consumableItem.getContent();

        {
            QuestOnlyComponent qc = c.getComponent(QuestOnlyComponent.class);
            if (qc != null)
            {
                GameMode gameMode = BrainOutClient.ClientController.getGameMode();

                if (gameMode != null && gameMode.getRealization() instanceof ClientFreeRealization)
                {
                    ClientFreeRealization free = ((ClientFreeRealization) gameMode.getRealization());

                    if (!free.isQuestActive(qc.getQuest()))
                    {
                        return;
                    }
                }
            }
        }

        InventoryItem item = newInventoryItem(record, inventoryDragAndDrop);

        if (preInitRecords != null)
        {
            preInitRecords.put(record.getRecord(), record);
            return;
        }

        records.put(record.getRecord(), item);
        dragAndDrop.addSource(item);

        if (placeholders.size > 0)
        {
            Placeholder ph = findPlaceholder(consumableItem.getContent());

            if (ph != null && ph.actors.size > 0)
            {
                Actor placeHolder = ph.actors.pop();
                contents.addActorBefore(placeHolder, item.getHolder());
                contents.removeActor(placeHolder);

                ph.required.add(placeHolder);
            }
        }
        else
        {
            addItemToContents(item);
        }
    }

    private void addItemToContents(InventoryItem item)
    {
        Actor extended = item.extendedSection(null);
        if (extended == null)
        {
            contents.addActor(item.getHolder());
        }
        else
        {
            Button h = item.getHolder();

            Group extendedWidget = new Group();

            extendedWidgets.put(h, extendedWidget);

            extendedWidget.addActor(h);

            extendedWidget.addActor(extended);

            if (item.extendedSectionToTheRight())
            {
                float w = item.extendedSectionWidth();

                extended.setBounds(h.getPrefWidth(), 0, w, h.getPrefHeight());
                extendedWidget.setBounds(0, 0, h.getPrefWidth() + w, h.getPrefHeight());
            }
            else
            {
                h.setBounds(0, h.getPrefHeight(), h.getPrefWidth(), h.getPrefHeight());
                extended.setBounds(0, 0, h.getPrefWidth(), h.getPrefHeight());
                extendedWidget.setBounds(0, 0, h.getPrefWidth(), h.getPrefHeight() * 2);
            }

            contents.addActor(extendedWidget);
        }
    }

    protected InventoryItem getItem(ConsumableRecord record)
    {
        return records.get(record);
    }

    protected void updateItem(ConsumableRecord record)
    {
        InventoryItem item = records.get(record);

        if (item == null)
            return;

        item.update();
    }

    protected boolean hasItem(ConsumableRecord record)
    {
        if (preInitRecords != null)
        {
            return preInitRecords.containsKey(record);
        }

        return records.containsKey(record);
    }

    public WidgetGroup getContents()
    {
        return contents;
    }

    protected void removeItem(ConsumableRecord record)
    {
        InventoryItem item = records.remove(record);

        if (item == null)
            return;

        if (placeholders.size > 0)
        {
            Placeholder ph = findPlaceholder(record.getItem().getContent());

            if (ph != null && ph.required.size > 0)
            {
                Actor placeHolder = ph.required.pop();
                ph.actors.add(placeHolder);

                contents.addActorAfter(item.getHolder(), placeHolder);
            }

            removeItemFromContents(item);
        }
        else
        {
            removeItemFromContents(item);
        }
    }

    private void removeItemFromContents(InventoryItem item)
    {
        Group extendedWidget = extendedWidgets.remove(item.getHolder());

        if (extendedWidget != null)
        {
            contents.removeActor(extendedWidget);
        }
        else
        {
            contents.removeActor(item.getHolder());
        }
    }

    protected InventoryItem newInventoryItem(InventoryRecord record, DragAndDropInventory inventory)
    {
        return new InventoryItem(record, inventory);
    }

    public void init()
    {
        if (preInitRecords.size > 0)
        {
            preInitRecords.orderedKeys().sort((o1, o2) -> getItemRank(o2) - getItemRank(o1));
        }

        for (ConsumableRecord record : preInitRecords.orderedKeys())
        {
            ConsumableItem consumableItem = record.getItem();

            if (consumableItem.isPrivate() && consumableItem.getPrivate() != BrainOutClient.ClientController.getMyId())
            {
                continue;
            }

            Content c = consumableItem.getContent();

            {
                QuestOnlyComponent qc = c.getComponent(QuestOnlyComponent.class);
                if (qc != null)
                {
                    GameMode gameMode = BrainOutClient.ClientController.getGameMode();

                    if (gameMode != null && gameMode.getRealization() instanceof ClientFreeRealization)
                    {
                        ClientFreeRealization free = ((ClientFreeRealization) gameMode.getRealization());

                        if (!free.isQuestActive(qc.getQuest()))
                        {
                            continue;
                        }
                    }
                }
            }

            InventoryItem item = newInventoryItem(preInitRecords.get(record), inventoryDragAndDrop);

            records.put(record, item);
            dragAndDrop.addSource(item);

            if (placeholders.size > 0)
            {
                Placeholder ph = findPlaceholder(consumableItem.getContent());

                if (ph != null && ph.actors.size > 0)
                {
                    Actor placeHolder = ph.actors.pop();
                    contents.addActorBefore(placeHolder, item.getHolder());
                    contents.removeActor(placeHolder);
                    ph.required.add(placeHolder);
                }
                else
                {
                    addItemToContents(item);
                }
            }
            else
            {
                addItemToContents(item);
            }
        }

        preInitRecords.clear();
        preInitRecords = null;

        updateBackgroundPanel(UpdateBackgroundPanelStatus.def);
    }

    public void release()
    {
        //
    }

    public interface ConsumableRecordPredicate
    {
        boolean test(ConsumableRecord record);
    }

    public void highlightStatus(InventoryRecord iv, boolean h)
    {

    }

    public void highlight(ConsumableRecordPredicate predicate)
    {
        for (ObjectMap.Entry<ConsumableRecord, InventoryItem> entry : records)
        {
            InventoryItem item = entry.value;

            item.getHolder().setStyle(BrainOutClient.Skin.get(
                predicate.test(entry.key) ? "button-highlighted" : getItemButtonStyle(),
                Button.ButtonStyle.class));
        }
    }

    protected boolean displayRecord(ConsumableRecord record)
    {
        return true;
    }

    public boolean hasCustomFriendlyButton()
    {
        return false;
    }

    public TextButton customFriendlyButton()
    {
        return null;
    }

    public ActiveData getPlaceInto()
    {
        return null;
    }

    public String getTitle()
    {
        return null;
    }

    public float getCurrentWeight()
    {
        return currentWeight;
    }

    public void refresh()
    {

    }
}
