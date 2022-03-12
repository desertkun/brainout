package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.common.msg.client.ContentActionMsg;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.Shop;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.content.upgrades.UpgradeChain;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.UserProfile;
import org.json.JSONObject;

public class UpgradeInventoryInstrumentMenu extends Menu
{
    private InstrumentSlotItem item;
    private InstrumentInfo info;
    private final ConsumableContainer playerInventory;
    private ConsumableRecord record;
    private InstrumentIcon instrumentIcon;
    private InstrumentCharacteristics chars;
    private Shader grayShader;
    private Button selectedSkinButton;
    private Table upgrades;
    private FreeplayInventoryUserPanel userPanel;

    public UpgradeInventoryInstrumentMenu(ConsumableRecord record,
                                          ConsumableContainer playerInventory)
    {
        this.playerInventory = playerInventory;
        this.record = record;

        fetchInfo();

        this.grayShader = ((Shader) BrainOut.ContentMgr.get("shader-grayed"));
    }

    private void fetchInfo()
    {
        if (record == null)
            return;

        ConsumableItem item = record.getItem();

        if (!(item instanceof InstrumentConsumableItem))
            return;

        InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);

        InstrumentData data = ici.getInstrumentData();
        Instrument instrument = data.getInstrument();
        this.item = instrument.getSlotItem();
        this.info = new InstrumentInfo(data.getInfo());
    }

    private boolean havePaintItem()
    {
        ConsumableContent paint = BrainOutClient.ContentMgr.get("consumable-item-paint", ConsumableContent.class);
        return playerInventory.hasConsumable(paint);
    }

    @Override
    public void render()
    {
        BrainOutClient.drawFade(0.5f, getBatch());

        super.render();
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-ingame");
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public Table createUI()
    {
        Table data = new Table();
        data.align(Align.center);

        upgrades = new Table();
        upgrades.align(Align.top);

        Table main = new Table();
        main.align(Align.top);
        userPanel = BrainOutClient.Env.createFreeplayInventoryUserPanel(playerInventory);

        data.add(userPanel).colspan(2).left().pad(20).expandX().fillX().row();

        data.add().colspan(2).expandY().row();

        data.add(upgrades).pad(10).expandX().right().top();
        data.add(main).pad(10).expandX().left().top().row();

        data.add().colspan(2).expandY().row();

        int w = 640, h = 192 + 64;

        this.instrumentIcon = new InstrumentIcon(info, 2.0f, true)
        {
            @Override
            protected float getCenterY()
            {
                return 0.6f;
            }
        };

        instrumentIcon.setBounds(0, 0, w, h);
        instrumentIcon.setCached(w, h);

        Group instrument = new Group();
        instrument.setBounds(0, 0, w, h);

        Shader blackShader = ((Shader) BrainOut.ContentMgr.get("shader-black"));

        Image shadedImage = new Image(instrumentIcon.getCachedTexture());
        shadedImage.setSize(w, h);

        ShaderedActor shaderedIcon = new ShaderedActor(shadedImage, blackShader);
        shaderedIcon.setPosition(8, -8);
        instrument.addActor(shaderedIcon);
        instrument.addActor(instrumentIcon);

        final UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        int skillLevel = userProfile.getInt(info.instrument.getSkillStat(), 0);

        Image weaponSkill = new Image(BrainOutClient.getRegion("weapon-skill-" + skillLevel + "-big"));
        weaponSkill.setTouchable(Touchable.disabled);
        weaponSkill.setScaling(Scaling.none);
        weaponSkill.setFillParent(true);
        weaponSkill.setAlign(Align.right | Align.top);
        weaponSkill.setPosition(-32, 8);

        instrument.addActor(weaponSkill);

        int kills = userProfile.getStats().get(info.instrument.getKillsStat(), 0.0f).intValue();

        if (kills > 0)
        {
            WidgetGroup killCountGroup = new WidgetGroup();

            Image killBg = new Image(BrainOutClient.getRegion("label-kills"));
            killBg.setScaling(Scaling.none);
            killBg.setFillParent(true);
            killCountGroup.addActor(killBg);

            Label killCount = new Label(String.valueOf(
                    kills),
                    BrainOutClient.Skin,
                    "title-small"
            );

            killCount.setAlignment(Align.center);
            killCount.setFillParent(true);
            killCountGroup.addActor(killCount);

            Table killCountParent = new Table();
            killCountParent.align(Align.left | Align.top);
            killCountParent.setTouchable(Touchable.childrenOnly);
            killCountParent.setFillParent(true);
            killCountParent.add(killCountGroup).size(64, 32).pad(10).row();

            instrument.addActor(killCountParent);
        }

        main.add(new BorderActor(new Label(item.getTitle().get(),
            BrainOutClient.Skin, "title-level"), "form-gray")).width(256).left().row();

        main.add(new BorderActor(instrument, "bg-weapon-panel")).expand().fill().size(w, h).row();

        renderSideButtons();

        if (havePaintItem())
        {
            Table skinButtons = new Table();
            skinButtons.align(Align.left | Align.bottom);
            skinButtons.setTouchable(Touchable.childrenOnly);
            skinButtons.setFillParent(true);

            renderSkinButtons(skinButtons);

            instrument.addActor(skinButtons);
        }

        instrumentIcon.init();

        Table buttons = new Table();
        buttons.align(Align.right);

        TextButton btnSave = new TextButton(L.get("MENU_SAVE"),
                BrainOutClient.Skin, "button-small");

        btnSave.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);

                pop();
            }
        });

        buttons.add(btnSave).size(192, 32);

        this.chars = new InstrumentCharacteristics(info, record.getQuality());

        Table charsActor = new Table();
        charsActor.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));
        charsActor.align(Align.center);
        charsActor.add(chars).expandX().fillX().pad(20);

        main.add(charsActor).size(w, h).row();

        main.add(buttons).expandX().fillX().row();

        return data;
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    private void renderSkinButtons(Table skinButtons)
    {
        final UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        ButtonGroup<Button> skinGroup = new ButtonGroup<>();
        skinGroup.setMinCheckCount(0);
        skinGroup.setMaxCheckCount(1);

        for (Skin skin : item.getSkins())
        {
            if (skin == item.getDefaultSkin())
                continue;

            if (!skin.hasItem(userProfile))
            {
                if (!skin.getLockItem().isVisible(userProfile))
                    continue;
            }

            final Button skinButton = new Button(BrainOutClient.Skin,
                    "button-upgrade-checkable");

            boolean locked = false;
            boolean unknown = false;

            if (skin.hasItem(userProfile))
            {
                skinButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        playSound(MenuSound.select);

                        Button prevButton = skinGroup.getChecked();

                        if (skinButton.isChecked())
                        {
                            selectSkin(skin, () -> {
                                //Analytics.EventDesign(1, "ui", "select-skin", skin.getID());
                                selectedSkinButton = skinButton;
                            }, ()->
                            {
                                Menu.playSound(MenuSound.denied);
                                if (prevButton != null)
                                {
                                    prevButton.setChecked(true);
                                }
                                else
                                {
                                    skinButton.setChecked(false);
                                }
                            } );
                        }
                        else
                        {
                            selectSkin(item.getDefaultSkin(), () -> {
                                selectedSkinButton = null;
                            }, () ->
                            {
                                Menu.playSound(MenuSound.denied);
                                skinButton.setChecked(true);
                            });
                        }
                    }
                });
            }
            else if (skin.isLocked(userProfile))
            {
                ContentLockTree.LockItem lockItem = skin.getLockItem();

                if (lockItem != null)
                {
                    unknown = true;
                }

                locked = true;

                skinButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        skinButton.setChecked(false);
                        playSound(MenuSound.denied);
                        //Analytics.EventDesign(1, "ui", "select-skin-denied", skin.getID());

                        if (selectedSkinButton != null)
                        {
                            selectedSkinButton.setChecked(true);
                        }
                    }
                });
            }
            else if (!Shop.getInstance().isFree(skin))
            {
                Shop.ShopItem shopItem = skin.getShopItem();

                skinButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        float have = userProfile.getStats().get(shopItem.getCurrency(), 0.0f);

                        if (have >= getPrice(shopItem))
                        {
                            playSound(MenuSound.select);

                            BrainOutClient.ClientController.sendTCP(new ContentActionMsg(
                                    skin, ContentActionMsg.Action.purchase
                            ));
                        }
                        else
                        {
                            playSound(MenuSound.denied);
                        }
                    }
                });

                skinButton.setDisabled(true);

                int amount = getPrice(shopItem);
                Image skillIcon = new Image(BrainOutClient.getRegion("skillpoints-small"));

                Table unlock = new Table();
                unlock.setTouchable(Touchable.disabled);
                unlock.setFillParent(true);
                unlock.align(Align.right | Align.bottom);

                unlock.add(new Label(String.valueOf(amount), BrainOutClient.Skin, "title-small")).pad(8);
                unlock.add(skillIcon).pad(8).padLeft(0).padBottom(4).padRight(12).row();

                skinButton.addActor(unlock);
            }

            if (skin.hasItem(userProfile))
            {
                Tooltip.RegisterToolTip(skinButton, skin.getTitle().get(), this);
            }
            else
            {
                UnlockTooltip.show(skinButton, skin, userProfile, this);
            }

            if (info.skin == skin)
            {
                selectedSkinButton = skinButton;
                skinButton.setChecked(true);
            }

            skinGroup.add(skinButton);

            IconComponent iconComponent = skin.getComponent(IconComponent.class);

            if (iconComponent != null)
            {
                TextureRegion region;

                if (unknown)
                {
                    region = BrainOutClient.getRegion("icon-skin-unknown");
                }
                else
                {
                    region = iconComponent.getIcon("big-icon");
                }

                Image icon = new Image(region);
                icon.setTouchable(Touchable.disabled);
                icon.setBounds(0, 0, 64, 32);
                icon.setOrigin(Align.center);

                final Actor iconActor;

                if (locked && !unknown)
                {
                    iconActor = new ShaderedActor(icon, grayShader);
                }
                else
                {
                    iconActor = icon;
                }

                iconActor.setBounds(2, 2, 64, 32);
                skinButton.addActor(iconActor);
            }

            MenuBadge.apply(skinButton, skin, MenuBadge.Mode.click);

            skinButtons.add(skinButton).size(68, 36).pad(8).padRight(0);
        }
    }

    private void selectSkin(Skin skin, Runnable success, Runnable failed)
    {
        JSONObject args = new JSONObject();
        args.put("object", record.getId());
        args.put("skin", skin.getID());

        WaitLoadingMenu loading = new WaitLoadingMenu("");
        pushMenu(loading);

        BrainOutClient.SocialController.sendRequest("freeplay_weapon_skin", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                if (response.has("record"))
                {
                    int recordId = response.optInt("record", -1);
                    setRecord(playerInventory.get(recordId));
                    fetchInfo();
                }

                loading.pop();

                instrumentIcon.setSkin(skin);
                success.run();
            }

            @Override
            public void error(String reason)
            {
                loading.pop();
                failed.run();
            }
        });
    }

    public void setRecord(ConsumableRecord record)
    {
        this.record = record;
        fetchInfo();
    }

    private boolean haveUpgrades()
    {
        GameMode gameMode = BrainOutClient.ClientController.getGameMode();

        if (gameMode != null && gameMode.getID() == GameMode.ID.free)
        {
            return true;
        }

        for (final ObjectMap.Entry<String, Array<Upgrade>> entry : item.getUpgrades())
        {
            boolean collapsable = true;

            for (final Upgrade upgrade : entry.value)
            {
                if (!(upgrade instanceof UpgradeChain.ChainedUpgrade))
                {
                    collapsable = false;
                    break;
                }
            }

            if (!collapsable)
            {
                return true;
            }

        }

        return false;
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        switch (keyCode)
        {
            case Input.Keys.TAB:
            {
                if (escape())
                {
                    return true;
                }
            }
        }

        return super.keyDown(keyCode);
    }

    private void renderSideButtons()
    {
        upgrades.clear();

        if (!haveUpgrades())
            return;

        upgrades.add(new BorderActor(new Label(L.get("MENU_UPGRADES"),
                BrainOutClient.Skin, "title-level"), "form-gray")).width(192).row();

        for (final ObjectMap.Entry<String, Array<Upgrade>> entry : item.getUpgrades())
        {
            final String key = entry.key;
            Array<Upgrade> upgrades_ = entry.value;

            boolean collapsable = true;

            for (final Upgrade upgrade : entry.value)
            {
                if (!(upgrade instanceof UpgradeChain.ChainedUpgrade))
                {
                    collapsable = false;
                    break;
                }
            }

            if (collapsable)
            {
                UpgradeChain.ChainedUpgrade required;
                UpgradeChain.ChainedUpgrade current = null;
                boolean found = false;

                Upgrade currentUpgrade = info.upgrades.get(key);
                int installed = upgrades_.indexOf(currentUpgrade, true);

                if (installed >= 0)
                {
                    if (installed < upgrades_.size - 1)
                    {
                        required = (UpgradeChain.ChainedUpgrade)upgrades_.get(installed + 1);
                    }
                    else
                    {
                        required = null;
                    }
                }
                else
                {
                    required = upgrades_.size > 0 ? (UpgradeChain.ChainedUpgrade)upgrades_.first() : null;
                }

                for (final Upgrade upgrade : upgrades_)
                {
                    if (info.upgrades.get(key) == upgrade)
                    {
                        current = (UpgradeChain.ChainedUpgrade)upgrade;
                        found = true;
                        continue;
                    }

                    if (found)
                    {
                        required = (UpgradeChain.ChainedUpgrade)upgrade;
                        break;
                    }
                }

                if (current != null)
                {
                    info.upgrades.put(key, current);
                }

                if (required != null)
                {
                    Button upgradeButton = addUpgradeButton(key, required, false, upgrades);

                    Tooltip.RegisterToolTip(upgradeButton,
                        new ChainedUpgradeUnlockTooltipCreator(key, required, upgrades_, info), this);
                }
            }
            else
            {
                for (final Upgrade upgrade : upgrades_)
                {
                    Button upgradeButton = addUpgradeButton(key, upgrade, true, upgrades);

                    if (upgradeButton == null)
                        continue;

                    Tooltip.RegisterStandardToolTip(
                        upgradeButton, upgrade.getTitle().get(), upgrade.getDescription().get(), this);
                }
            }
        }
    }

    private Button addUpgradeButton(
            String key, Upgrade upgrade,
            boolean canBeSelected,
            Table sideButtons)
    {
        Shop.ShopItem shopItem = upgrade.getShopItem();

        if (shopItem == null)
            return null;

        final Button upgradeButton = new Button(BrainOutClient.Skin,
            canBeSelected ? "button-upgrade-checkable" : "button-upgrade");

        class HoverUpgradeListener extends ClickOverListener
        {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
            {
                super.enter(event, x, y, pointer, fromActor);

                chars.setHoverUpgrade(key, upgrade);
                updateChars();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
            {
                super.exit(event, x, y, pointer, toActor);

                chars.clearHoverUpgrade();
                updateChars();
            }
        }

        ConsumableContent junk = BrainOutClient.ContentMgr.get("consumable-item-junk", ConsumableContent.class);

        if (info.upgrades.get(key) == upgrade)
        {
            if (canBeSelected)
            {
                upgradeButton.setChecked(true);
            }

            upgradeButton.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    upgradeButton.setChecked(true);
                }
            });
        }
        else
        {
            upgradeButton.addListener(new HoverUpgradeListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    if (playerInventory.getAmount(junk) >= getPrice(shopItem))
                    {
                        installUpgrade(key, upgrade, () ->
                        {
                            instrumentIcon.enableUpgrade(key, upgrade);
                            updateChars();
                            renderSideButtons();
                            userPanel.refresh();

                        },  () ->
                        {
                            upgradeButton.setChecked(false);
                            playSound(MenuSound.denied);
                        });
                    }
                    else
                    {
                        upgradeButton.setChecked(false);
                        playSound(MenuSound.denied);
                    }
                }
            });

            int amount = getPrice(shopItem);
            String icon = "icon-junk-small";
            Image currencyIcon = new Image(BrainOutClient.getRegion(icon));

            Table price = new Table();
            price.setTouchable(Touchable.disabled);
            price.setFillParent(true);
            price.align(Align.right | Align.bottom);

            price.add(new Label(String.valueOf(amount), BrainOutClient.Skin,
                playerInventory.getAmount(junk) >= getPrice(shopItem) ? "title-small" : "title-red")).pad(8);
            price.add(currencyIcon).pad(8).padLeft(-8).padBottom(4).padRight(2).row();

            upgradeButton.addActor(price);
        }

        IconComponent iconComponent = upgrade.getComponent(IconComponent.class);

        if (iconComponent != null)
        {
            TextureRegion region = iconComponent.getIcon("big-icon");

            Image icon = new Image(region);
            icon.setScaling(Scaling.none);
            icon.setTouchable(Touchable.disabled);
            icon.setScale(2.0f);
            icon.setBounds(0, 0, 192, 64);
            icon.setOrigin(Align.center);

            upgradeButton.addActor(icon);
        }

        sideButtons.add(upgradeButton).size(192, 64).row();

        return upgradeButton;
    }

    private int getPrice(Shop.ShopItem shopItem)
    {
        if (shopItem.getCurrency().equals(Constants.User.GEARS))
            return 1;

        return shopItem.getAmount();
    }

    private void installUpgrade(String key, Upgrade upgrade, Runnable success, Runnable failed)
    {
        JSONObject args = new JSONObject();
        args.put("object", record.getId());
        args.put("key", key);
        args.put("upgrade", upgrade.getID());

        WaitLoadingMenu loading = new WaitLoadingMenu("");
        pushMenu(loading);

        BrainOutClient.SocialController.sendRequest("freeplay_weapon_upgrade", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                if (response.has("record"))
                {
                    int recordId = response.optInt("record", -1);
                    record = playerInventory.get(recordId);
                }

                loading.pop();
                success.run();
            }

            @Override
            public void error(String reason)
            {
                loading.pop();
                failed.run();
            }
        });
    }

    private void updateChars()
    {
        chars.update();
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        instrumentIcon.dispose();
    }

    @Override
    public boolean lockRender()
    {
        return true;
    }

    private static class ChainedUpgradeUnlockTooltipCreator extends UnlockTooltip.UnlockTooltipCreator
    {
        private final Array<Upgrade> upgrades;
        private final String key;
        private final InstrumentInfo info;
        private final Upgrade upgrade;

        public ChainedUpgradeUnlockTooltipCreator(
            String key,
            Upgrade upgrade,
            Array<Upgrade> upgrades,
            InstrumentInfo info)
        {
            super(null, null);

            this.key = key;
            this.upgrades = upgrades;
            this.info = info;
            this.upgrade = upgrade;
        }

        @Override
        protected String getLockedIcon(ContentLockTree.LockItem lockItem)
        {
            String unlockFor = lockItem.getUnlockFor();

            if (Constants.User.TECH_LEVEL.equals(unlockFor))
            {
                Levels levels = BrainOutClient.ClientController.getLevels(unlockFor);
                Levels.Level level = levels.getLevel(lockItem.getParam());

                return level.icon;
            }

            return super.getLockedIcon(lockItem);
        }

        @Override
        protected String getTitle()
        {
            String level = String.valueOf(getUpgradesLevel(key, upgrades, info) + 1);
            return upgrade.getTitle().get() + " (" + L.get("MENU_LEVEL_N", level) + ")";
        }

        @Override
        protected String getDescription()
        {
            return upgrade.getDescription().get();
        }

        @Override
        protected Actor renderBottomLine()
        {
            Table parts = new Table();

            int have = getUpgradesLevel(key, upgrades, info);

            int i = 0;

            boolean short_ = upgrades.size > 5;

            for (final Upgrade upgrade : upgrades)
            {
                String icon;

                if (i < have)
                {
                    icon = short_ ? "icon-chained-upgrade-short-have" : "icon-chained-upgrade-have";
                }
                else if (i == have)
                {
                    icon = short_ ? "icon-chained-upgrade-short-available" : "icon-chained-upgrade-available";
                }
                else
                {
                    icon = short_ ? "icon-chained-upgrade-short-locked" : "icon-chained-upgrade-locked";
                }

                Image image = new Image(BrainOutClient.getRegion(icon));
                image.setScaling(Scaling.none);
                parts.add(image).pad(8);

                i++;
            }

            return parts;
        }

        @Override
        protected boolean forceBottomLine()
        {
            return true;
        }
    }

    private static int getUpgradesLevel(String key, Array<Upgrade> upgrades, InstrumentInfo info)
    {
        Upgrade upgrade = info.upgrades.get(key);

        int i = 1;

        for (final Upgrade test : upgrades)
        {
            if (test == upgrade)
            {
                return i;
            }

            i++;
        }

        return 0;
    }

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
