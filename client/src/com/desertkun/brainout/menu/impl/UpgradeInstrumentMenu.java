package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.NotifyData;
import com.desertkun.brainout.common.enums.data.ContentND;
import com.desertkun.brainout.common.msg.client.ContentActionMsg;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.Skin;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.InstrumentAnimationComponent;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.Shop;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.content.upgrades.UpgradeChain;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.events.NotifyEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.MenuUtils;
import org.json.JSONObject;

public class UpgradeInstrumentMenu extends Menu implements EventReceiver
{
    private final InstrumentSlotItem item;
    private final InstrumentSlotItem.InstrumentSelection selection;
    private final InstrumentInfo info;
    private final Runnable done;
    private OwnableContent toInstall;
    private InstrumentIcon instrumentIcon;
    private InstrumentCharacteristics chars;
    private Shader grayShader;
    private Button selectedSkinButton;

    public UpgradeInstrumentMenu(InstrumentSlotItem item, Runnable done)
    {
        this.done = done;
        this.item = item;
        this.selection = ((InstrumentSlotItem.InstrumentSelection) item.getStaticSelection());

        this.info = new InstrumentInfo();

        this.info.instrument = item.getInstrument();
        this.info.skin = selection.getInfo().skin;
        this.toInstall = null;

        this.grayShader = ((Shader) BrainOut.ContentMgr.get("shader-grayed"));
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
    public void onInit()
    {
        super.onInit();

        BrainOutClient.EventMgr.subscribe(Event.ID.notify, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
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


        Table main = new Table();
        main.align(Align.top);

        data.add(BrainOutClient.Env.createTechUserPanel()).colspan(2).left().pad(20).expandX().fillX().row();

        data.add().colspan(2).expandY().row();

        if (item.getUpgrades().size > 0)
        {
            Table upgrades = new Table();
            upgrades.align(Align.top);
            data.add(upgrades).pad(10).expandX().right().top();
            data.add(main).pad(10).expandX().left().top().row();
            upgrades.add(new BorderActor(new Label(L.get("MENU_UPGRADES"),
                BrainOutClient.Skin, "title-level"), "form-gray")).width(192).row();

            renderSideButtons(upgrades);
        }
        else
        {
            data.add(main).pad(10).expandX().center().top().row();
        }


        data.add().colspan(2).expandY().row();


        int w = 640, h = 192 + 64;

        float scale;

        InstrumentAnimationComponent iac =
            info.instrument.getComponentFrom(InstrumentAnimationComponent.class);

        if (iac != null)
        {
            scale = iac.getIconScale();
        }
        else
        {
            scale = 1.0f;
        }

        this.instrumentIcon = new InstrumentIcon(info, scale * 2.0f, true)
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

        Table skinButtons = new Table();
        skinButtons.align(Align.left | Align.bottom);
        skinButtons.setTouchable(Touchable.childrenOnly);
        skinButtons.setFillParent(true);

        renderSkinButtons(skinButtons);

        instrument.addActor(skinButtons);

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

                close();
            }
        });

        buttons.add(btnSave).size(192, 32);

        this.chars = new InstrumentCharacteristics(info, -1);

        Table charsActor = new Table();
        charsActor.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));
        charsActor.align(Align.center);
        charsActor.add(chars).expandX().fillX().pad(20);

        main.add(charsActor).size(w, h).row();

        main.add(buttons).expandX().fillX().row();

        return data;
    }

    private void close()
    {
        if (done != null)
        {
            done.run();
        }

        pop();
    }

    private void renderSkinButtons(Table skinButtons)
    {
        final UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        ButtonGroup<Button> skinGroup = new ButtonGroup<>();
        skinGroup.setMinCheckCount(0);
        skinGroup.setMaxCheckCount(1);

        for (Skin skin : item.getSkins())
        {
            if (skin == null)
                continue;

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
                if (toInstall == skin)
                {
                    toInstall = null;

                    instrumentIcon.setSkin(skin);
                    selection.setSkin(skin);
                }

                skinButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        playSound(MenuSound.select);

                        Sound sound = ((Sound) BrainOutClient.ContentMgr.get("skin-owned-snd"));

                        if (sound != null)
                        {
                            sound.play();
                        }

                        if (skinButton.isChecked())
                        {
                            //Analytics.EventDesign(1, "ui", "select-skin", skin.getID());

                            selectedSkinButton = skinButton;
                            instrumentIcon.setSkin(skin);
                            selection.setSkin(skin);
                        } else
                        {
                            selectedSkinButton = null;
                            instrumentIcon.setSkin(item.getDefaultSkin());
                            selection.setSkin(item.getDefaultSkin());
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

                if (!BrainOut.OnlineEnabled())
                {
                    skinButton.addListener(new ActorGestureListener()
                    {
                        @Override
                        public void tap(InputEvent event, float x, float y, int count, int button)
                        {
                            if (count == 2)
                            {
                                JSONObject args = new JSONObject();
                                args.put("content", skin.getID());

                                BrainOutClient.SocialController.sendRequest("offline_force_unlock", args,
                                    new SocialController.RequestCallback()
                                {
                                    @Override
                                    public void success(JSONObject response)
                                    {
                                        UpgradeInstrumentMenu.this.reset();
                                    }

                                    @Override
                                    public void error(String reason)
                                    {
                                        System.out.println(reason);
                                    }
                                });
                            }
                        }
                    });
                }

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

                        if (have >= shopItem.getAmount())
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

                int amount = shopItem.getAmount();
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

            if (selection.getSelectedSkin() == skin)
            {
                selectedSkinButton = skinButton;
                skinButton.setChecked(true);
                info.skin = skin;
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

                if (region != null)
                {

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
                else
                {
                    System.err.println("Error: Cannot find skin icon " + skin.getID());
                }
            }

            MenuBadge.apply(skinButton, skin, MenuBadge.Mode.click);

            skinButtons.add(skinButton).size(68, 36).pad(8).padRight(0);
        }
    }

    private void renderSideButtons(Table sideButtons)
    {
        final UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        ObjectMap<String, ButtonGroup<Button>> groups = new ObjectMap<>();

        for (final ObjectMap.Entry<String, Array<Upgrade>> entry : item.getUpgrades())
        {
            final String key = entry.key;

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
                UpgradeChain.ChainedUpgrade required = entry.value.size > 0 ? (UpgradeChain.ChainedUpgrade)entry.value.peek() : null;
                UpgradeChain.ChainedUpgrade current = null;

                for (final Upgrade upgrade : entry.value)
                {
                    if (upgrade.hasItem(userProfile))
                    {
                        current = (UpgradeChain.ChainedUpgrade)upgrade;
                        continue;
                    }

                    required = (UpgradeChain.ChainedUpgrade)upgrade;
                    break;
                }

                if (current != null)
                {
                    selection.getSelectedUpgrades().put(key, current);
                    info.upgrades.put(key, current);
                }

                if (required != null)
                {
                    Button upgradeButton = addUpgradeButton(key, required, null, false, false, sideButtons);

                    if (upgradeButton != null)
                        Tooltip.RegisterToolTip(upgradeButton,
                            new ChainedUpgradeUnlockTooltipCreator(required, userProfile, entry.value), this);
                }
            }
            else
            {
                for (final Upgrade upgrade : entry.value)
                {
                    ButtonGroup<Button> group = groups.get(key);

                    if (group == null)
                    {
                        group = new ButtonGroup<>();
                        group.setMaxCheckCount(1);
                        group.setMinCheckCount(0);
                        groups.put(key, group);
                    }

                    Button upgradeButton = addUpgradeButton(key, upgrade, group, true, true, sideButtons);
                    if (upgradeButton != null)
                        UnlockTooltip.show(upgradeButton, upgrade, userProfile, this);
                }
            }
        }
    }

    private Button addUpgradeButton(
            String key, Upgrade upgrade,
            ButtonGroup<Button> selectionGroup,
            boolean canBeSelected,
            boolean shade, Table sideButtons)
    {
        final UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

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

        boolean locked = false;

        if (upgrade.hasItem(userProfile))
        {
            if (toInstall == upgrade)
            {
                toInstall = null;

                //Analytics.EventDesign(1, "ui", "select-upgrade", upgrade.getID());

                instrumentIcon.enableUpgrade(key, upgrade);
                selection.getSelectedUpgrades().put(key, upgrade);
            }

            upgradeButton.addListener(new HoverUpgradeListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    playSound(MenuSound.select);

                    if (canBeSelected)
                    {
                        if (upgradeButton.isChecked())
                        {
                            //Analytics.EventDesign(1, "ui", "select-upgrade", upgrade.getID());

                            instrumentIcon.enableUpgrade(key, upgrade);
                            selection.getSelectedUpgrades().put(key, upgrade);
                            updateChars();
                        } else
                        {
                            instrumentIcon.disableUpgrade(key);
                            selection.getSelectedUpgrades().remove(key);
                            updateChars();
                        }
                    }
                }
            });
        }
        else if (upgrade.isLocked(userProfile))
        {
            locked = true;

            upgradeButton.addListener(new HoverUpgradeListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    //Analytics.EventDesign(1, "ui", "select-upgrade-denied", upgrade.getID());

                    upgradeButton.setChecked(false);
                    playSound(MenuSound.denied);
                }
            });

            if (!BrainOut.OnlineEnabled())
            {
                upgradeButton.addListener(new ActorGestureListener()
                {
                    @Override
                    public void tap(InputEvent event, float x, float y, int count, int button)
                    {
                        if (count == 2)
                        {
                            JSONObject args = new JSONObject();
                            args.put("content", upgrade.getID());

                            BrainOutClient.SocialController.sendRequest("offline_force_unlock", args,
                                    new SocialController.RequestCallback()
                                    {
                                        @Override
                                        public void success(JSONObject response)
                                        {
                                            UpgradeInstrumentMenu.this.reset();
                                        }

                                        @Override
                                        public void error(String reason)
                                        {
                                            System.out.println(reason);
                                        }
                                    });
                        }
                    }
                });
            }

            ContentLockTree.LockItem lockItem = upgrade.getLockItem();

            if (lockItem != null)
            {
                if (lockItem.getUnlockFor().equals(Constants.User.TECH_LEVEL))
                {
                    Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.TECH_LEVEL);
                    Levels.Level level = levels.getLevel(lockItem.getParam());

                    Image techIcon = new Image(BrainOutClient.getRegion("instrument-upgrades"));
                    techIcon.setColor(Color.RED);

                    Label required = new Label(level.toShortString(),
                            BrainOutClient.Skin, "title-small");
                    required.setColor(Color.RED);

                    Table unlock = new Table();
                    unlock.setTouchable(Touchable.disabled);
                    unlock.setFillParent(true);
                    unlock.align(Align.left | Align.bottom);

                    unlock.add(techIcon).pad(8).padBottom(4).padRight(-2);
                    unlock.add(required).pad(8).row();

                    upgradeButton.addActor(unlock);
                }
            }
        }
        else if (!upgrade.isFree())
        {
            Shop.ShopItem shopItem = upgrade.getShopItem();

            upgradeButton.addListener(new HoverUpgradeListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    float have = userProfile.getStats().get(shopItem.getCurrency(), 0.0f);

                    if (have >= shopItem.getAmount())
                    {
                        playSound(MenuSound.select);

                        BrainOutClient.ClientController.sendTCP(new ContentActionMsg(
                            upgrade, ContentActionMsg.Action.purchase
                        ));
                    }
                    else
                    {
                        playSound(MenuSound.denied);
                    }
                }
            });

            upgradeButton.setDisabled(true);

            int amount = shopItem.getAmount();
            String icon = MenuUtils.getStatIcon(shopItem.getCurrency());
            Image currencyIcon = new Image(BrainOutClient.getRegion(icon));

            Table unlock = new Table();
            unlock.setTouchable(Touchable.disabled);
            unlock.setFillParent(true);
            unlock.align(Align.right | Align.bottom);

            unlock.add(new Label(String.valueOf(amount), BrainOutClient.Skin, "title-small")).pad(8);
            unlock.add(currencyIcon).pad(8).padLeft(0).padBottom(4).padRight(12).row();

            upgradeButton.addActor(unlock);
        }

        if (selection.getSelectedUpgrades().get(key) == upgrade)
        {
            if (canBeSelected)
            {
                upgradeButton.setChecked(true);
            }
            info.upgrades.put(key, upgrade);
        }

        if (selectionGroup != null)
        {
            selectionGroup.add(upgradeButton);
        }

        IconComponent iconComponent = upgrade.getComponent(IconComponent.class);

        if (iconComponent != null)
        {
            TextureRegion region = iconComponent.getIcon("big-icon");

            if (region != null)
            {
                Image icon = new Image(region);
                icon.setScaling(Scaling.none);
                icon.setTouchable(Touchable.disabled);
                icon.setScale(2.0f);
                icon.setBounds(0, 0, 192, 64);
                icon.setOrigin(Align.center);

                final Actor iconActor;

                if (locked && shade)
                {
                    iconActor = new ShaderedActor(icon, grayShader);
                }
                else
                {
                    iconActor = icon;
                }

                iconActor.setBounds(0, 0, 192, 64);
                upgradeButton.addActor(iconActor);
            }
            else
            {
                System.err.println("Error: Cannot find upgrade icon " + upgrade.getID());
            }
        }

        sideButtons.add(upgradeButton).size(192, 64).row();

        MenuBadge.apply(upgradeButton, upgrade);

        return upgradeButton;
    }

    private void updateChars()
    {
        chars.update();
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.notify, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOutClient.EventMgr.unsubscribeAll(Event.ID.badgeRead);

        instrumentIcon.dispose();
    }

    @Override
    public boolean lockRender()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                SimpleEvent simpleEvent = ((SimpleEvent) event);

                switch (simpleEvent.getAction())
                {
                    case userProfileUpdated:
                    {
                        userProfileUpdated();

                        break;
                    }
                }

                break;
            }
            case notify:
            {
                NotifyEvent notifyEvent = ((NotifyEvent) event);

                notify(notifyEvent);

                break;
            }
        }

        return false;
    }

    private void userProfileUpdated()
    {
        reset();
    }

    private void notify(NotifyEvent event)
    {
        if (event.notifyAward == NotifyAward.ownable &&
                event.method == NotifyMethod.install &&
                event.reason == NotifyReason.gotOwnable)
        {
            NotifyData notifyData = event.data;

            if (notifyData instanceof ContentND)
            {
                ContentND contentND = ((ContentND) notifyData);

                OwnableContent ownableContent = ((OwnableContent) BrainOut.ContentMgr.get(contentND.id));

                gotOwnable(ownableContent);
            }
        }
    }

    private void gotOwnable(OwnableContent ownableContent)
    {
        if (ownableContent instanceof Upgrade)
        {
            toInstall = ownableContent;

            if (ownableContent instanceof UpgradeChain.ChainedUpgrade)
            {
                playSound(MenuSound.repair);
            }
            else
            {
                playSound(MenuSound.install);
            }

            reset();
        }
    }

    private static class ChainedUpgradeUnlockTooltipCreator extends UnlockTooltip.UnlockTooltipCreator
    {
        private final UserProfile userProfile;
        private final Array<Upgrade> upgrades;
        private final UpgradeChain.ChainedUpgrade upgrade;

        public ChainedUpgradeUnlockTooltipCreator(
            UpgradeChain.ChainedUpgrade upgrade, UserProfile userProfile,
            Array<Upgrade> upgrades)
        {
            super(upgrade, userProfile);

            this.upgrade = upgrade;
            this.userProfile = userProfile;
            this.upgrades = upgrades;
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
            String level = String.valueOf(upgrade.getItemLevel(userProfile));
            return super.getTitle() + " (" + L.get("MENU_LEVEL_N", level) + ")";
        }

        @Override
        protected Actor renderBottomLine()
        {
            Table parts = new Table();

            int have = 0;

            for (final Upgrade upgrade : upgrades)
            {
                if (upgrade.hasItem(userProfile))
                {
                    have++;
                }
            }

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

    @Override
    public boolean lockInput()
    {
        return true;
    }
}
