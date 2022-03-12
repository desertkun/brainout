package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.settings.KeyProperties;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.ContentND;
import com.desertkun.brainout.common.enums.data.NotifyData;
import com.desertkun.brainout.common.msg.client.*;
import com.desertkun.brainout.components.DurabilityComponent;
import com.desertkun.brainout.components.ShowPurchaseProgressComponent;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.components.*;
import com.desertkun.brainout.content.gamecase.Case;
import com.desertkun.brainout.content.gamecase.gen.ContentCard;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.*;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.WithBadge;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.ConfirmationPopup;
import com.desertkun.brainout.menu.popups.OKInputPopup;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.mode.ClientGameRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.*;
import com.desertkun.brainout.posteffects.effects.ChromaticAbberationPostEffect;
import com.desertkun.brainout.utils.*;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.SocialService;
import org.json.JSONObject;

import java.util.Objects;


public class PlayerSelectionMenu extends Menu implements EventReceiver
{
    private final Shop shop;
    private final ShopCart shopCart;

    private Slot selectedSlot;
    private ObjectMap<Slot, Slot.Category> selectedCategory;
    private ObjectMap<Slot, String> selectedTag;
    private Array<InstrumentIcon> activeIcons;
    private Layout selectedLayout;
    private ButtonGroup<TextButton> slotButtonsGroup;

    public enum UserPanelMode
    {
        standard,
        tech
    }

    protected enum OpenWindowMode
    {
        none,
        slot,
        blog
    }

    protected UserPanel userPanel;
    private Content repaired;
    private Tabs.Tab trophiesTab;
    private UserPanelMode userPanelMode;

    private Table customContent;
    private Table playerSkinSlot;
    private Table eventsContent;
    private Table playerClanContent;
    private Table playerInfo;
    private Table mainContent;
    private OpenWindowMode openWindowMode;

    protected Table slotsContent;
    protected Table layoutContent;

    public enum EventType
    {
        daily,
        group,
        shootingRange,
        valuables,
        battlePass
    }

    public PlayerSelectionMenu(ShopCart shopCart)
    {
        this.shopCart = shopCart;
        this.shop = Shop.getInstance();

        this.openWindowMode = OpenWindowMode.none;
        this.userPanelMode = UserPanelMode.standard;
        this.selectedSlot = null;
        this.selectedTag = null;
        this.selectedCategory = new ObjectMap<>();
        this.activeIcons = new Array<>();
        this.selectedTag = new ObjectMap<>();

        shopCart.init();

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();
        selectedLayout = userProfile.getLayout();

        if (selectedLayout == null)
        {
            selectedLayout = BrainOutClient.ContentMgr.get("layout-1", Layout.class);
            if (selectedLayout != null)
            {
                userProfile.setLayout(selectedLayout);
            }
        }

        initSlots();

        if (selectedLayout != null)
        {
            shopCart.setLayout(selectedLayout.getID());
        }

        updateShopCardSelection();

        Gdx.app.postRunnable(this::checkUserProfile);

    }

    public class ContentProgressTooltipCreator extends UnlockTooltip.UnlockTooltipCreator
    {
        public ContentProgressTooltipCreator(OwnableContent item, UserProfile userProfile)
        {
            super(item, userProfile);
        }

        @Override
        protected boolean forceBottomLine()
        {
            return true;
        }

        @Override
        protected boolean removeBottomLine()
        {
            return !userProfile.hasItem(item);
        }

        private ContentProgressComponent getProgressComponent()
        {
            return item.getComponent(ContentProgressComponent.class);
        }

        @Override
        protected void renderItem(Table content, OwnableContent item)
        {
            ContentProgressComponent pro = getProgressComponent();

            renderReward(content);

            Label taskText = new Label(L.get("MENU_TASK"), BrainOutClient.Skin, "title-gray");
            taskText.setAlignment(Align.center);
            taskText.setWrap(true);
            content.add(taskText).expandX().fillX().pad(8).row();

            Label goal = new Label(pro.getGoal().get(String.valueOf(pro.getValue())),
                BrainOutClient.Skin, "title-small");
            goal.setAlignment(Align.center);
            goal.setWrap(true);

            content.add(goal).expandX().fillX().padLeft(16).padRight(16).row();
        }

        private void renderReward(Table content)
        {
            Table section = new Table();
            content.add(section).pad(16).row();
            {
                AddStatItemComponent addStat = item.getComponent(AddStatItemComponent.class);
                if (addStat != null)
                {
                    ContentImage.RenderStatImage(addStat.getStat(), addStat.getAmount(), section);
                }
            }
            {
                UnlockStoreItemComponent unlock = item.getComponent(UnlockStoreItemComponent.class);
                if (unlock != null)
                {
                    ContentImage.RenderImage(unlock.getContent(), section, unlock.getAmount());
                }
            }
        }

        @Override
        protected Actor renderBottomLine()
        {
            if (userProfile == null || item == null)
                return null;

            ContentProgressComponent pro = getProgressComponent();

            int need = pro.getValue();
            int have = Math.min((int)(float)userProfile.getStats().get(pro.getStat(), 0.0f), pro.getValue());

            Group progress = new Group();

            ProgressBar scoreBar = new ProgressBar(0, need,
                    1, false, BrainOutClient.Skin,
                    "progress-score");

            scoreBar.setBounds(
                0,
                -1,
                512,
                ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT
            );

            scoreBar.setValue(have);
            progress.addActor(scoreBar);

            Label scoreValue = new Label(String.valueOf(have) + " / " + need,
                    BrainOutClient.Skin, "title-small");

            scoreValue.setAlignment(Align.center);
            scoreValue.setFillParent(true);
            progress.addActor(scoreValue);

            return progress;
        }
    }

    private void initSlots()
    {
        BrainOut.ContentMgr.iterateContent(Slot.class, check ->
        {
            initSlot(check);
            return false;
        });
    }

    private void initSlot(Slot slot)
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        {
            String categorySelection = userProfile.getSelection(slot.getCategorySelectionId());
            if (categorySelection != null && !"".equals(categorySelection))
            {
                Slot.Category category = slot.getCategory(categorySelection);

                if (category != null)
                {
                    selectedCategory.put(slot, category);
                }
            }
        }

        {
            String tagSelection = userProfile.getSelection(slot.getTagSelectionId());
            if (tagSelection != null && !"".equals(tagSelection))
            {
                Slot.Tag tag = slot.getTag(tagSelection);

                if (tag != null)
                {
                    selectedTag.put(slot, tag.getId());
                }
            }
        }
    }

    public ShopCart getShopCart()
    {
        return shopCart;
    }

    private void updateShopCardSelection()
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();
        shopCart.initStaticSelection(userProfile, selectedLayout);
    }

    private void save()
    {
        final CSGame gc = BrainOutClient.ClientController.getState(CSGame.class);

        if (gc == null)
            return;

        gc.saveSelection(shopCart);
    }

    public static MenuBadge.BadgeCreator ITEM_BADGE = new MenuBadge.DefaultBadgeCreator()
    {
        @Override
        public WithBadge.Involve involveChild()
        {
            return WithBadge.Involve.itemOnly;
        }
    };

    public static MenuBadge.BadgeCreator UPGRADE_BADGE = new MenuBadge.BadgeCreator()
    {
        @Override
        public MenuBadge get(WidgetGroup to, WithBadge badge, UserProfile profile, MenuBadge.Mode mode)
        {
            return new MenuBadge(to, badge, profile, mode)
            {
                @Override
                protected void removeBadge(Actor applied)
                {
                    ((ImageButton) to).setStyle(
                            BrainOutClient.Skin.get("button-upgrades", ImageButton.ImageButtonStyle.class)
                    );
                }

                @Override
                protected Actor applyBadge(WidgetGroup to)
                {
                    ((ImageButton) to).setStyle(
                            BrainOutClient.Skin.get("button-upgrades-badge", ImageButton.ImageButtonStyle.class)
                    );

                    return this;
                }
            };
        }

        @Override
        public WithBadge.Involve involveChild()
        {
            return WithBadge.Involve.childOnly;
        }
    };

    @Override
    public Table createUI()
    {
        Table data = new Table();

        Table primaryContent = new Table();
        Table topContent = new Table();
        Table rightContent = new Table();

        this.customContent = new Table();
        this.mainContent = new Table();
        this.mainContent.align(Align.top);
        this.slotsContent = new Table();
        this.slotsContent.align(Align.right);
        this.layoutContent = new Table();
        this.layoutContent.align(Align.right);

        this.playerClanContent = new Table();
        this.playerSkinSlot = new Table();
        this.eventsContent = new Table();
        this.playerInfo = new Table();
        this.playerInfo.align(Align.left);

        updateShopCardSelection();
        updateInfo();

        Table slice = new Table();

        slice.add(primaryContent).expand().fill();
        slice.add(rightContent).padTop(-8).expandY().fillY().row();

        setUpRightPanel(rightContent);

        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
        if (csGame != null && !csGame.isSpectator())
        {
            setUpMainPanel(primaryContent);

            setUpTopPanel(topContent);
        }

        data.add(topContent).expandX().fillX().row();
        data.add(slice).expand().fill().row();

        if (shouldOpenSlotAtStart() && getShop() != null && getShop().getOpen() != null)
        {
            setPostEffect();

            Preset preset = BrainOutClient.ClientController.getPreset();

            for (Slot slot : getShop().getOpen())
            {
                if (preset != null)
                {
                    if (!preset.isSlotAllowed(slot))
                    {
                        continue;
                    }
                }

                selectedSlot = slot;
                renderSlot(selectedSlot);

                break;
            }
        }

        return data;
    }

    private void setUpTopPanel(Table topPanel)
    {
        topPanel.add(playerInfo).pad(8).expandX().fillX().left().top().row();
    }

    protected void setUpMainPanel(Table primaryContent)
    {
        primaryContent.add(mainContent).padTop(8).width(729).expand().fill().colspan(3).row();

        primaryContent.add(customContent).bottom().left();
        primaryContent.add(layoutContent).padRight(-8).padBottom(8).expandX().right().bottom();
        primaryContent.add(slotsContent).fillX().row();
    }

    protected void setUpRightPanel(Table rightContent)
    {
        rightContent.add(eventsContent).padRight(8).padTop(8).width(192).expandX().fillX().right().row();
        rightContent.add(playerClanContent).padRight(8).width(192).expand().fillX().right().top().row();
    }

    protected void updateInfo()
    {
        updateEvensInfo();
        updatePlayerInfo();
        updateSlotButtons();
        updatePlayerClanInfo();
    }

    private void updatePlayerClanInfo()
    {
        playerClanContent.clear();

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        OwnableContent clanPass = BrainOutClient.ContentMgr.get(
            ClientConstants.Other.CLAN_PASS,
            OwnableContent.class);

        if (clanPass == null)
            return;

        if (clanPass.getLockItem() != null && !clanPass.getLockItem().isUnlocked(userProfile))
        {
            return;
        }

        Label skinTitle = new Label(L.get("MENU_CLAN"),
                BrainOutClient.Skin, "title-small");
        skinTitle.setAlignment(Align.center);
        playerClanContent.add(new BorderActor(skinTitle, "form-gray")).size(192, 32).expandX().fillX().row();

        Button button = new Button(BrainOutClient.Skin, "button-notext");
        button.setBackground("form-default");

        Image avatarImage;

        if (userProfile.isParticipatingClan())
        {
            avatarImage = new Image();

            Avatars.Get(userProfile.getClanAvatar(),
                (has, avatar) ->
            {
                if (has)
                {
                    avatarImage.setDrawable(new TextureRegionDrawable(new TextureRegion(avatar)));
                }
                else
                {
                    avatarImage.setDrawable(BrainOutClient.Skin, "default-avatar");
                }
            });
        }
        else
        {
            avatarImage = new Image(BrainOutClient.Skin, "default-avatar");
        }

        button.add(avatarImage).size(60, 60).expandX().center();

        Clan myClan = BrainOutClient.SocialController.getMyClan();

        if (myClan != null)
        {
            if (myClan.isInConflict())
            {
                avatarImage.addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                    Actions.delay(0.25f),
                    Actions.moveBy(0, 8, 0.25f, Interpolation.circleOut),
                    Actions.moveBy(0, -8, 1.0f, Interpolation.bounceOut)
                )));

                {
                    Image vs = new Image(BrainOutClient.Skin, "icon-vs");
                    button.add(vs).size(24, 24).pad(4);
                }

                SocialService socialService = SocialService.Get();
                LoginService loginService = LoginService.Get();

                if (socialService != null && loginService != null)
                {
                    socialService.getGroup(loginService.getCurrentAccessToken(), myClan.getConflictWith(),
                    (service, request, result, group) -> Gdx.app.postRunnable(() ->
                    {
                        if (result != Request.Result.success)
                            return;

                        Gdx.app.postRunnable(() ->
                        {
                            String avatar = group.getProfile().optString("avatar");

                            if (avatar == null || avatar.isEmpty())
                            {
                                Image versus = new Image(BrainOutClient.Skin, "default-avatar");

                                versus.addAction(Actions.sequence(
                                        Actions.delay(0.25f),
                                        Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                                                Actions.delay(0.25f),
                                                Actions.moveBy(0, 8, 0.25f, Interpolation.circleOut),
                                                Actions.moveBy(0, -8, 1.0f, Interpolation.bounceOut)
                                        ))
                                ));

                                button.add(versus).size(60, 60).expandX().center();

                                return;
                            }

                            Avatars.Get(avatar, (has, avatar1) ->
                            {
                                Image versus;

                                if (has)
                                {
                                    versus = new Image(new TextureRegionDrawable(
                                        new TextureRegion(avatar1)));
                                }
                                else
                                {
                                    versus = new Image(BrainOutClient.Skin, "default-avatar");
                                }

                                versus.addAction(Actions.sequence(
                                    Actions.delay(0.25f),
                                    Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                                            Actions.delay(0.25f),
                                            Actions.moveBy(0, 8, 0.25f, Interpolation.circleOut),
                                            Actions.moveBy(0, -8, 1.0f, Interpolation.bounceOut)
                                    ))
                                ));

                                button.add(versus).size(60, 60).expandX().center();
                            });
                        });
                    }));
                }
            }
        }

        button.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                notReady();

                if (userProfile.isParticipatingClan())
                {
                    pushMenu(new ClanMenu(userProfile.getClanId()));
                }
                else
                {
                    pushMenu(new BrowseClansMenu());
                }
            }
        });

        playerClanContent.add(button).expandX().fillX().padBottom(4).height(64).row();
    }

    private void checkRestrictions()
    {
        shopCart.checkRestrictions();
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    private void renderSlotButton(Slot slot, Table slotRoot, ButtonGroup<TextButton> buttonGroup)
    {
        Table item = new Table();

        final TextButton click = new TextButton("", BrainOutClient.Skin, "button-checkable");

        click.setFillParent(true);
        item.addActor(click);

        click.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                playSound(MenuSound.select);

                clickSlot(slot);
            }
        });

        if (buttonGroup != null)
        {
            buttonGroup.add(click);
        }

        SlotItem.Selection selection = shopCart.getItem(slot);
        SlotItem slotItem = selection.getItem();

        if (slotItem.hasComponent(IconComponent.class))
        {
            TextureAtlas.AtlasRegion icon = slotItem.getComponent(IconComponent.class).getIcon();
            if (icon != null)
            {
                Image image = new Image(icon);
                image.setScaling(Scaling.none);
                image.setTouchable(Touchable.disabled);
                item.add(image).padRight(8);
            }
        }
        else if (selection instanceof InstrumentSlotItem.InstrumentSelection)
        {
            InstrumentSlotItem.InstrumentSelection iselection =
                    ((InstrumentSlotItem.InstrumentSelection) selection);

            float scale;

            InstrumentAnimationComponent iac = iselection.getInfo().instrument.
                getComponentFrom(InstrumentAnimationComponent.class);

            if (iac != null)
            {
                scale = iac.getIconScale();
            }
            else
            {
                scale = 1.0f;
            }

            InstrumentIcon icon = new InstrumentIcon(iselection.getInfo(), scale, false);
            icon.init();
            item.add(icon);
        }
        else
        {
            Label label = new Label(slotItem.getTitle().get(), BrainOutClient.Skin, "title-small");
            label.setTouchable(Touchable.disabled);

            item.add(label);
        }

        slotRoot.add(item).expand().fill().row();
        MenuBadge.apply(click, slot);
    }

    private void updateSlotButtons()
    {
        if (shop == null)
            return;

        checkRestrictions();

        slotsContent.clear();
        playerSkinSlot.clear();

        slotButtonsGroup = new ButtonGroup<>();
        slotButtonsGroup.setMinCheckCount(0);

        renderLayouts();

        customContent.clear();
        updateSlotContents(customContent, slotsContent, slotButtonsGroup);

        Table stack = null;
        float stackWidth = 0;
        int stacked = 0;

        Preset preset = BrainOutClient.ClientController.getPreset();

        for (final Slot slot: shop.getSlots())
        {
            if (!slot.isVisible())
            {
                continue;
            }

            if (preset != null && !preset.isSlotAllowed(slot))
                continue;

            float width = slot.getWidth();

            Table slotRoot = new Table();
            renderSlotButton(slot, slotRoot, slotButtonsGroup);

            if (slot.isStack())
            {
                if (stack == null || stackWidth != slot.getWidth() || stacked >= 2)
                {
                    stack = new Table();
                    stacked = 0;
                    stackWidth = slot.getWidth();
                    slotsContent.add(stack).width(width).bottom().pad(8, 8, 8, 0);
                }

                if (stackWidth != 0 && stackWidth != slot.getWidth())
                {
                    stackWidth = 0;
                    stack = null;
                    slotsContent.add(slotRoot).size(width, 64).bottom().pad(8, 8, 8, 0);
                }
                else
                {
                    stack.add(slotRoot).size(width, 64).row();
                    stacked++;
                }
            }
            else
            {
                stackWidth = 0;
                stack = null;
                slotsContent.add(slotRoot).size(width, 64).bottom().pad(8, 8, 8, 0);
            }
        }

    }

    private void renderLayouts()
    {
        layoutContent.clear();

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        Array<Layout> layouts =
            BrainOutClient.ContentMgr.queryContentTpl(Layout.class, check -> !check.isLocked(userProfile));

        if (layouts.size < 2)
            return;

        layouts.sort((o1, o2) -> o2.getOrder() - o1.getOrder());

        ButtonGroup<TextButton> layoutsButtons = new ButtonGroup<>();
        layoutsButtons.setMinCheckCount(1);
        layoutsButtons.setMaxCheckCount(1);

        for (Layout layout : layouts)
        {
            final Layout l_ = layout;

            String title = layout.getKey().isEmpty() ? "1" : layout.getKey();
            TextButton btn = new TextButton(title, BrainOutClient.Skin, "button-checkable");

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    selectLayout(l_);
                }
            });

            layoutsButtons.add(btn);
            layoutContent.add(btn).size(32, 32).row();

            if (selectedLayout == layout)
            {
                btn.setChecked(true);
            }
        }
    }

    private void selectLayout(Layout layout)
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        if (layout.isLocked(userProfile))
            return;

        userProfile.setLayout(layout);
        selectedLayout = layout;

        updateShopCardSelection();
        updateSlotButtons();

        save();
    }

    protected void updateSlotContents(Table custom, Table slotsContent, ButtonGroup<TextButton> buttonGroup)
    {

    }

    protected void setPostEffect()
    {
    }

    protected void releasePostEffect()
    {
    }

    protected void closeSlotInfo()
    {
        setOpenWindowMode(OpenWindowMode.none);

        mainContent.clear();

        if (selectedSlot != null)
        {
            releasePostEffect();
        }

        selectedSlot = null;
    }

    private void clickSlot(Slot slot)
    {
        //Analytics.EventDesign(1, "ui", "select-slot", slot.getID());

        if (selectedSlot != slot)
        {
            setStandardUserPanel();

            if (selectedSlot == null)
            {
                setPostEffect();
            }

            addSwitchPostEffect();
            renderSlot(slot);

            selectedSlot = slot;
        }
        else
        {
            closeSlotInfo();

            selectedSlot = null;
        }
    }

    private void addSwitchPostEffect()
    {
        if (BrainOutClient.ClientSett.isShaderEffectsEnabled())
        {
            addDelayedPostEffect(new ChromaticAbberationPostEffect(), 0.25f);
        }
    }

    private void refreshSlotInfo()
    {
        if (selectedSlot != null)
        {
            renderSlot(selectedSlot);
        }
    }

    protected void renderSlot(final Slot slot)
    {
        setOpenWindowMode(OpenWindowMode.slot);

        mainContent.clear();

        Tabs tabs = new Tabs(BrainOutClient.Skin);
        tabs.addListener(new Tabs.TabSelectedListener()
        {
            @Override
            public boolean selected()
            {
                setScrollFocus(((ScrollPane) tabs.getCurrentTab().getUserObject()));
                addSwitchPostEffect();
                return false;
            }
        });

        if (!slot.hasTags())
        {
            mainContent.add().width(77);
        }
        else
        {
            Table tagsTable = new Table();

            ButtonGroup<Button> tags = new ButtonGroup<>();

            tags.setMinCheckCount(1);
            tags.setMaxCheckCount(1);

            {
                TextButton all = new TextButton(L.get("SLOT_TAG_ALL"), BrainOutClient.Skin, "button-checkable");
                tags.add(all);
                tagsTable.add(all).size(77, 32).row();

                if (selectedTag.get(slot) == null)
                {
                    all.setChecked(true);
                }

                all.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        selectedTag.remove(slot);
                        refreshCurrentSlotCategories(tabs);

                        sendSelectTag(slot, null);
                    }
                });
            }

            {
                Button favorites = new Button(BrainOutClient.Skin, "button-checkable");

                Image icon = new Image(BrainOutClient.Skin, "icon-favorite-on");
                icon.setScaling(Scaling.none);
                favorites.add(icon);

                tags.add(favorites);
                tagsTable.add(favorites).size(77, 32).row();

                if (Constants.Other.FAVORITES_TAG.equals(selectedTag.get(slot)))
                {
                    favorites.setChecked(true);
                }

                favorites.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        selectedTag.put(slot, Constants.Other.FAVORITES_TAG);
                        refreshCurrentSlotCategories(tabs);

                        sendSelectFavorites(slot);
                    }
                });

                Tooltip.RegisterToolTip(favorites, L.get("MENU_FAVORITES"), this);
            }

            Preset preset = BrainOutClient.ClientController.getPreset();

            for (final Slot.Tag tag : slot.getTags())
            {
                if (preset != null && !preset.isTagAllowed(slot, tag.getId()))
                    continue;

                Button tagButton = new Button(BrainOutClient.Skin, "button-checkable");

                Image icon = new Image(BrainOutClient.Skin, "icon-tag-" + tag.getId());
                icon.setScaling(Scaling.none);
                tagButton.add(icon);

                tags.add(tagButton);
                tagsTable.add(tagButton).size(77, 32).padTop(2).row();

                tagButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);
                        selectedTag.put(slot, tag.getId());

                        sendSelectTag(slot, tag);
                        refreshCurrentSlotCategories(tabs);
                    }
                });

                Tooltip.RegisterToolTip(tagButton, tag.getTitle().get(), this);

                String selectedTag_ = selectedTag.get(slot);

                if (selectedTag_ != null && selectedTag_.equals(tag.getId()))
                {
                    tagButton.setChecked(true);
                }
            }

            mainContent.add(tagsTable).width(77);
        }

        mainContent.add(tabs).expandX().fillX().row();

        Preset preset = BrainOutClient.ClientController.getPreset();

        boolean forceFirst = selectedCategory.get(slot) == null;

        {
            Slot.Category category = selectedCategory.get(slot);

            if (category != null && preset != null && !preset.isCategoryHasAllowedItem(slot, category))
            {
                forceFirst = true;
            }
        }

        for (final Slot.Category category: slot.getCategories())
        {
            if (preset != null && !preset.isCategoryHasAllowedItem(slot, category))
                continue;

            if (forceFirst)
            {
                forceFirst = false;

                selectedCategory.put(slot, category);
            }

            Tabs.Tab tab = tabs.addTab(category.getTitle().get(), category).size(116, 32).padRight(8);

            MenuBadge.apply(tab.getTabButton(), category);

            tab.addListener(new Tabs.TabSelectedListener()
            {
                @Override
                public boolean selected()
                {
                    setStandardUserPanel();

                    selectedSlot = slot;
                    selectedCategory.put(slot, category);

                    sendSelectCategory(slot, category);

                    return false;
                }
            });

            renderSlotCategory(tab, category, slot);
        }

        Slot.Category selected = selectedCategory.get(slot);

        if (selected != null)
        {
            tabs.selectTab(selected);
        }

        if (isExchangeSlot(slot))
        {
            Table newContainer = new Table();
            newContainer.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-gray")));

            UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

            if (userProfile != null
                    && userProfile.getStats().get(Constants.User.NUCLEAR_MATERIAL, 0.0f)
                        < Constants.DailyReward.MAX_DAILY_CONTAINERS)
            {
                if (userProfile.getItems().get(Constants.User.DAILY_CONTAINER, 0) >= 1)
                {
                    Label takeIt = new Label(L.get("DAILY_CONTAINER_NEEDS_OPEN"), BrainOutClient.Skin, "title-yellow");
                    newContainer.add(takeIt).pad(4);
                }
                else
                {
                    long now = System.currentTimeMillis() / 1000L;

                    if (now >= userProfile.getLastDailyClaim())
                    {
                        Label takeIt = new Label(L.get("DAILY_CONTAINER_AVAILABLE"), BrainOutClient.Skin, "title-yellow");

                        newContainer.add(takeIt);
                    }
                    else
                    {
                        Label available = new Label(
                                L.get("DAILY_CONTAINER_APPEAR_IN"), BrainOutClient.Skin, "title-yellow");

                        newContainer.add(available).padRight(4);

                        Label timer = new Label("", BrainOutClient.Skin, "title-small");

                        timer.addAction(Actions.forever(Actions.sequence(
                                Actions.run(() ->
                                {
                                    long now1 = System.currentTimeMillis() / 1000L;
                                    long time = Math.max(userProfile.getLastDailyClaim() - now1, 0);

                                    timer.setText(DurationUtils.GetDurationString((int)time));
                                }),
                                Actions.delay(1)
                        )));

                        newContainer.add(timer);
                    }
                }
            }

            mainContent.add();
            mainContent.add(newContainer).expandX().fillX().row();
        }
        else
        {
            trophiesTab = tabs.addTabIcon("icon-trophies").size(32, 32);
            trophiesTab.getTabCell().expandX().right();

            trophiesTab.addListener(new Tabs.TabSelectedListener()
            {
                @Override
                public boolean selected()
                {
                    //Analytics.EventDesign(1, "ui", "select-trophies-tab");

                    selectedSlot = null;
                    setTechUserPanel();

                    return false;
                }
            });

            MenuBadge.apply(trophiesTab.getTabButton(), new WithBadge()
            {
                @Override
                public boolean hasBadge(UserProfile profile, Involve involve)
                {
                    for (ObjectMap.Entry<Integer, Trophy> entry :
                            BrainOutClient.ClientController.getUserProfile().getTrophies())
                    {
                        if (entry.value.hasBadge(profile, involve))
                        {
                            return true;
                        }
                    }

                    return false;
                }

                @Override
                public String getBadgeId()
                {
                    return null;
                }
            });

            renderTrophies();
        }
    }

    private void sendSelectTag(Slot slot, Slot.Tag tag)
    {
        BrainOutClient.ClientController.sendTCP(new SelectTagMsg(slot, tag));
    }

    private void setFavorite(UserProfile userProfile, SlotItem slot, boolean fav)
    {
        if (fav)
        {
            userProfile.addFavorite(slot.getID());
        }
        else
        {
            userProfile.removeFavorite(slot.getID());
        }

        BrainOutClient.ClientController.sendTCP(new SetFavoriteMsg(slot, fav));
    }

    private void sendSelectFavorites(Slot slot)
    {
        BrainOutClient.ClientController.sendTCP(new SelectFavoritesMsg(slot));
    }

    private void sendSelectCategory(Slot slot, Slot.Category category)
    {
        BrainOutClient.ClientController.sendTCP(new SelectCategoryMsg(slot, category));
    }

    private boolean isExchangeSlot(Slot slot)
    {
        return Objects.equals(slot.getID(), Constants.User.STORE_SLOT);
    }

    private void refreshCurrentSlotCategories(Tabs tabs)
    {
        if (selectedSlot == null)
            return;

        for (Slot.Category category : selectedSlot.getCategories())
        {
            Tabs.Tab tab = tabs.findTab(category);

            if (tab == null)
                continue;

            renderSlotCategory(tab, category, selectedSlot);
        }
    }

    private void renderTrophies()
    {
        if (trophiesTab == null)
            return;

        trophiesTab.clearChildren();

        Table content = new Table();
        Table items = new Table();

        UserProfile profile = BrainOutClient.ClientController.getUserProfile();

        if (profile != null)
        {
            for (int i = 0; i < UserProfile.MAX_TROPHIES; i++)
            {
                Trophy trophy = profile.getTrophy(i);

                Table slotItemContainer = new Table();
                slotItemContainer.align(Align.bottom);

                final TextButton button = new TextButton("", BrainOutClient.Skin, "button-default");
                button.setFillParent(true);

                Image bg = new Image(BrainOutClient.getRegion("icon-trophy-bg"));
                bg.setScaling(Scaling.none);
                bg.setFillParent(true);
                button.addActor(bg);

                slotItemContainer.addActor(button);

                if (trophy != null && trophy.getInfo() != null && trophy.getInfo().instrument != null)
                {
                    InstrumentIcon icon = new InstrumentIcon(trophy.getInfo(), 1.0f, true);
                    icon.init();

                    activeIcons.add(icon);

                    icon.setTouchable(Touchable.disabled);
                    icon.setFillParent(true);

                    slotItemContainer.addActor(icon);

                    com.desertkun.brainout.menu.ui.Tooltip.RegisterToolTip(button, () ->
                    {
                        Table tooltip = new com.desertkun.brainout.menu.ui.Tooltip.TooltipTable();

                        Table tooltipContent = new Table();
                        tooltipContent.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

                        Levels levels = BrainOutClient.ClientController.getLevels(
                                Constants.User.LEVEL
                        );
                        Levels.Level level = levels.getLevel(trophy.getOwnerLevel());

                        Image levelIcon = new Image(BrainOutClient.getRegion(level.icon));
                        levelIcon.setScaling(Scaling.none);

                        tooltipContent.add(levelIcon).size(40).pad(4).row();

                        tooltipContent.add(new Label(L.get("MENU_TROPHY_OF_PLAYER"),
                                BrainOutClient.Skin, "title-small")).row();

                        tooltipContent.add(new Label(trophy.getOwnerName(),
                                BrainOutClient.Skin, "title-yellow")).row();

                        tooltipContent.add(new Label(L.get("MENU_TROPHY_PRESS"),
                                BrainOutClient.Skin, "title-gray")).row();

                        Label title = new Label(trophy.getInfo().skin.getTitle().get(),
                                BrainOutClient.Skin, "title-level");
                        title.setAlignment(Align.center);
                        title.setWrap(true);

                        tooltip.add(new BorderActor(title, "form-gray")).expandX().fillX().row();
                        tooltip.add(tooltipContent).expand().fill().row();

                        tooltip.setSize(544, 240);

                        return tooltip;

                    }, this);

                    button.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            //Analytics.EventDesign(1, "ui", "disassemble-trophy-popup");

                            pushMenu(new ConfirmationPopup()
                            {
                                @Override
                                public String getTitle()
                                {
                                    return L.get("MENU_TROPHY_DISASSEMBLE_TITLE");
                                }

                                @Override
                                protected void initContent(Table data)
                                {
                                    data.add(new RichLabel(
                                            L.get("MENU_TROPHY_TO_DISASSEMBLE",
                                                    trophy.getInfo().skin.getTitle().get(),
                                                    String.valueOf(trophy.getXp())),
                                            BrainOutClient.Skin, "title-small")).pad(32).row();
                                }

                                @Override
                                public String buttonNo()
                                {
                                    return L.get("MENU_CANCEL");
                                }

                                @Override
                                public String buttonYes()
                                {
                                    return L.get("MENU_OK");
                                }

                                @Override
                                public void yes()
                                {
                                    //Analytics.EventDesign(trophy.getXp(), "ui", "disassemble-trophy");

                                    Menu.playSound(MenuSound.repair);

                                    BrainOutClient.ClientController.sendTCP(
                                            new DisassembleTrophyMsg(trophy.getIndex())
                                    );
                                }
                            });
                        }
                    });

                    MenuBadge.apply(button, trophy, MenuBadge.Mode.hover);
                }

                items.add(slotItemContainer).size(204, 64).pad(2);

                if (i % 3 == 2)
                {
                    items.row();
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(items, BrainOutClient.Skin, "scroll-default");
        scrollPane.setFadeScrollBars(false);
        content.add(scrollPane).padTop(8).padBottom(8).expandX().fillX().row();

        {
            Label b = new Label(L.get("MENU_TROPHIES_DESC"), BrainOutClient.Skin, "title-small");
            b.setWrap(true);
            b.setAlignment(Align.center);
            content.add(b).padTop(8).padBottom(8).expandX().fillX().center().row();
        }

        trophiesTab.add(content).maxHeight(360).expandX().fillX().row();
    }

    private void renderSlotCategory(Tabs.Tab tab, final Slot.Category category, Slot slot)
    {
        tab.clearChildren();

        Shader grayShader = ((Shader) BrainOut.ContentMgr.get("shader-grayed"));

        Table content = new Table();
        content.align(Align.top | Align.left);
        Table items = new Table();

        activeIcons.clear();

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        String selectedTag = this.selectedTag.get(slot);

        Preset preset = BrainOutClient.ClientController.getPreset();

        int i = 0;
        for (final SlotItem item: category.getItems())
        {
            if (preset != null && !preset.isItemAllowed(item))
                continue;

            if (selectedTag != null)
            {
                if (Constants.Other.FAVORITES_TAG.equals(selectedTag))
                {
                    if (!userProfile.isFavorite(item.getID()))
                        continue;
                }
                else
                {
                    if (!item.hasTag(selectedTag))
                        continue;
                }
            }

            ItemLimitsComponent limits = item.getComponent(ItemLimitsComponent.class);

            if (limits != null && !limits.getLimits().passes(userProfile))
                continue;

            HideIfOwnedComponent hio = item.getComponent(HideIfOwnedComponent.class);

            if (hio != null && hio.hide(userProfile, slot))
                continue;

            {
                UnlockStoreItemComponent unlock = item.getComponentFrom(UnlockStoreItemComponent.class);

                if (unlock != null)
                {
                    OwnableContent ownableContent = unlock.getContent();

                    if (ownableContent instanceof Case)
                    {
                        Case asCase = ((Case) ownableContent);

                        if (!asCase.applicable(userProfile))
                            continue;
                    }

                    if (unlock.isUnique())
                    {
                        if (userProfile.itemsHave(ownableContent) > 0)
                        {
                            continue;
                        }
                    }
                }
            }

            if (item instanceof StoreSlotItem)
            {
                StoreSlotItem storeSlotItem = ((StoreSlotItem) item);

                if (storeSlotItem.getLimit() > 0)
                {
                    if (!userProfile.checkLimit(item.getID()))
                    {
                        continue;
                    }
                }
            }

            Table slotItemContainer = new Table();
            slotItemContainer.align(Align.bottom);

            final TextButton button = new TextButton("", BrainOutClient.Skin, "button-checkable");

            if (item instanceof StoreSlotItem)
            {
                Image bg = new Image(BrainOutClient.getRegion("store-item-bg"));
                bg.setScaling(Scaling.none);
                bg.setFillParent(true);
                button.addActor(bg);
            }

            button.setFillParent(true);

            final boolean available = !item.isLocked(userProfile) || item.hasItem(userProfile);

            slotItemContainer.addActor(button);

            if (!available)
            {
                ContentLockTree.LockItem lockItem = item.getLockItem();

                if (lockItem != null && !lockItem.isVisible(userProfile))
                {
                    continue;
                }


            }

            CornerButtons corners = new CornerButtons();
            corners.setFillParent(true);
            slotItemContainer.addActor(corners);

            if (item.hasComponent(IconComponent.class))
            {
                TextureAtlas.AtlasRegion icon = item.getComponent(IconComponent.class).getIcon();

                if (icon != null)
                {
                    Image image = new Image(icon);

                    image.setTouchable(Touchable.disabled);
                    image.setFillParent(true);
                    image.setScaling(Scaling.none);

                    if (available)
                    {
                        slotItemContainer.addActor(image);
                    }
                    else
                    {
                        ShaderedActor sh = new ShaderedActor(image, grayShader);
                        sh.setFillParent(true);
                        sh.setTouchable(Touchable.disabled);

                        slotItemContainer.addActor(sh);
                    }
                }
            }
            else if (item instanceof InstrumentSlotItem)
            {
                InstrumentSlotItem slotItem = ((InstrumentSlotItem) item);

                InstrumentSlotItem.InstrumentSelection selection =
                        ((InstrumentSlotItem.InstrumentSelection) slotItem.getStaticSelection());

                if (selection.getInfo() != null && selection.getInfo().instrument != null)
                {
                    float scale;

                    InstrumentAnimationComponent iac = slotItem.getInstrument().
                        getComponentFrom(InstrumentAnimationComponent.class);

                    if (iac != null)
                    {
                        scale = iac.getIconScale();
                    }
                    else
                    {
                        scale = 1.0f;
                    }

                    InstrumentIcon icon = new InstrumentIcon(selection.getInfo(), scale, true);
                    icon.init();

                    activeIcons.add(icon);

                    icon.setTouchable(Touchable.disabled);
                    icon.setFillParent(true);

                    if (available)
                    {
                        slotItemContainer.addActor(icon);
                    }
                    else
                    {
                        ShaderedActor sh;

                        if (item.hasComponent(PartialSlotItemComponent.class))
                        {
                            ContentLockTree.LockItem lockItem = item.getLockItem();

                            if (lockItem != null)
                            {
                                int value = lockItem.getUnlockValue(userProfile, 0);

                                if (value == 0)
                                {
                                    sh = new ShaderedActor(icon, grayShader);
                                }
                                else
                                {
                                    sh = new PartialShaderedActor(icon, grayShader,
                                            value, lockItem.getParam());
                                }
                            } else
                            {
                                sh = new ShaderedActor(icon, grayShader);
                            }
                        } else
                        {
                            sh = new ShaderedActor(icon, grayShader);
                        }

                        sh.setFillParent(true);
                        sh.setTouchable(Touchable.disabled);

                        slotItemContainer.addActor(sh);

                        ShowPurchaseProgressComponent spp = item.getComponent(ShowPurchaseProgressComponent.class);

                        if (spp != null && spp.show(slot))
                        {
                            Shop.ShopItem shopItem = item.getShopItem();

                            int have = (int) (float) userProfile.getStats().get(shopItem.getCurrency(), 0.0f);

                            slotItemContainer.addActor(new ButtonProgressBar(have, shopItem.getAmount(),
                                    BrainOutClient.Skin, "progress-parts"));
                        }
                    }


                    if (!item.hasItem(userProfile))
                    {
                        if (item.hasComponent(PartialSlotItemComponent.class))
                        {
                            ContentLockTree.LockItem lockItem = item.getLockItem();

                            if (lockItem != null)
                            {
                                int value = lockItem.getUnlockValue(userProfile, 0);

                                if (value != 0)
                                {
                                    slotItemContainer.addActor(new ButtonProgressBar(value, lockItem.getParam(),
                                            BrainOutClient.Skin, "progress-parts"));
                                }
                            }
                        }
                    }
                }

            }

            if (available)
            {
                {
                    ContentProgressComponent spp = item.getComponent(ContentProgressComponent.class);

                    if (spp != null)
                    {
                        int have = (int) (float) userProfile.getStats().get(spp.getStat(), 0.0f);

                        slotItemContainer.addActor(new ButtonProgressBar(have, spp.getValue(),
                                BrainOutClient.Skin, "progress-parts"));
                    }
                }

                if (BrainOut.R.instanceOf(InstrumentSlotItem.class, item))
                {
                    final InstrumentSlotItem islot = ((InstrumentSlotItem) item);

                    if (item.hasItem(userProfile))
                    {
                        if (islot.getUpgrades().size > 0 || islot.getSkins().size > 1)
                        {
                            ImageButton upgradeButton = new ImageButton(BrainOutClient.Skin,
                                    "button-upgrades");

                            upgradeButton.addListener(new ClickOverListener()
                            {
                                @Override
                                public void clicked(InputEvent event, float x, float y)
                                {
                                    if (item.getRestriction() != null)
                                    {
                                        SlotItem.Restriction r = item.getRestriction();
                                        SlotItem.Selection selection = shopCart.getItem(r.slot);
                                        if (selection == null || selection.getItem() != r.item)
                                        {
                                            //Analytics.EventDesign(1, "ui", "open-upgrade-menu-restricted", item.getID());

                                            upgradeButton.setChecked(false);
                                            playSound(MenuSound.denied);
                                            return;
                                        }
                                    }

                                    //Analytics.EventDesign(1, "ui", "open-upgrade-menu", item.getID());

                                    playSound(MenuSound.select);

                                    //Analytics.EventDesign(1, "ui", "select-item", item.getID());
                                    selectItem(item.getSlot(), item.getStaticSelection());
                                    save();

                                    pushMenu(new UpgradeInstrumentMenu(islot, PlayerSelectionMenu.this::save));
                                }
                            });

                            MenuBadge.apply(upgradeButton, islot,
                                    MenuBadge.Mode.click, UPGRADE_BADGE);

                            corners.setCorner(CornerButtons.Corner.bottomLeft, upgradeButton, 24, 24);
                        }
                    }

                    Instrument instrument = islot.getInstrument();

                    if (instrument != null)
                    {
                        if (BrainOut.R.instanceOf(Weapon.class, instrument))
                        {
                            Weapon weapon = ((Weapon) instrument);

                            if (weapon.getSkills().size > 0)
                            {
                                String stat = weapon.getSkillStat();

                                int skillLevel = BrainOutClient.ClientController.getUserProfile().getInt(stat, 0);

                                SkillsButton skillButton = new SkillsButton(skillLevel,
                                        weapon, islot.getTitle().get(), this);

                                corners.setCorner(CornerButtons.Corner.topRight, skillButton, 24, 24);
                            }
                        }

                        DurabilityComponent wdc = instrument.getComponentFrom(DurabilityComponent.class);

                        if (wdc != null)
                        {
                            float durability = wdc.getDurability(BrainOutClient.ClientController.getUserProfile());

                            if (wdc.isEnoughtToFix(durability))
                            {
                                GearsButton fixButton = new GearsButton(instrument, islot.getTitle().get(), this);

                                corners.setCorner(CornerButtons.Corner.bottomRight, fixButton, 24, 24);

                                if (repaired == instrument)
                                {
                                    repaired = null;

                                    fixButton.setColor(1, 1, 1, 0);
                                    fixButton.addAction(Actions.sequence(
                                            Actions.delay(0.25f),
                                            Actions.color(Color.WHITE)
                                    ));
                                }
                            }
                        }
                    }
                }

                if (item.hasItem(userProfile) && !(item instanceof StoreSlotItem))
                {
                    button.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            if (item.getRestriction() != null)
                            {
                                SlotItem.Restriction r = item.getRestriction();
                                SlotItem.Selection selection = shopCart.getItem(r.slot);
                                if (selection == null || selection.getItem() != r.item)
                                {
                                    button.setChecked(false);
                                    playSound(MenuSound.denied);

                                    //Analytics.EventDesign(1, "ui", "open-upgrade-menu-restricted", item.getID());

                                    return;
                                }
                            }

                            playSound(MenuSound.select);

                            //Analytics.EventDesign(1, "ui", "select-item", item.getID());
                            selectItem(item.getSlot(), item.getStaticSelection());
                            save();

                            closeSlotInfo();
                            selectedSlot = null;
                            updateSlotButtons();
                        }
                    });
                }
                else
                {
                    Shop.ShopItem shopItem = item.getShopItem();

                    if (shopItem != null)
                    {
                        int amount = shopItem.getAmount();
                        String amountString = String.valueOf(amount);

                        Image skillIcon;

                        if (shopItem.getCurrency().equals("ru"))
                        {
                            amountString = amountString + " RU";
                            skillIcon = null;
                        }
                        else
                        {
                            String icon = MenuUtils.getStatIcon(shopItem.getCurrency());
                            skillIcon = new Image(BrainOutClient.getRegion(icon));
                        }

                        String priceStyle = "title-small";

                        if (shopItem.getCurrency().equals(Constants.User.NUCLEAR_MATERIAL))
                        {
                            priceStyle = "title-green";
                        }

                        Table unlock = new Table();
                        unlock.setTouchable(Touchable.disabled);
                        unlock.setFillParent(true);
                        unlock.align(Align.right | Align.bottom);

                        unlock.add(new Label(amountString, BrainOutClient.Skin, priceStyle)).pad(8);

                        if (skillIcon != null)
                        {
                            unlock.add(skillIcon).pad(8).padLeft(0).padBottom(4).padRight(12).row();
                        }

                        slotItemContainer.addActor(unlock);

                        button.addListener(new ClickOverListener()
                        {
                            @Override
                            public void clicked(InputEvent event, float x, float y)
                            {
                                button.setChecked(false);

                                float have = userProfile.getStats().get(shopItem.getCurrency(), 0.0f);

                                if (have >= shopItem.getAmount())
                                {
                                    playSound(MenuSound.select);

                                    purchaseItem(item);
                                } else
                                {
                                    playSound(MenuSound.denied);
                                }
                            }
                        });

                        if (item instanceof StoreSlotItem)
                        {
                            int have = (int)(float)userProfile.getStats().get(shopItem.getCurrency(), 0.0f);

                            slotItemContainer.addActor(new ButtonProgressBar(have, shopItem.getAmount(),
                                    BrainOutClient.Skin, "progress-parts"));
                        }
                        else
                        {
                            ShowPurchaseProgressComponent spp = item.getComponent(ShowPurchaseProgressComponent.class);

                            if (spp != null && spp.show(slot))
                            {
                                int have = (int) (float) userProfile.getStats().get(shopItem.getCurrency(), 0.0f);

                                slotItemContainer.addActor(new ButtonProgressBar(have, shopItem.getAmount(),
                                        BrainOutClient.Skin, "progress-parts"));
                            }
                        }
                    }

                }
            }
            else
            {
                if (BrainOut.OnlineEnabled())
                {

                    button.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            button.setChecked(false);

                            if (lockedItemClicked(item))
                            {
                                Menu.playSound(MenuSound.select);
                            }
                            else
                            {
                                playSound(MenuSound.denied);
                            }
                        }
                    });
                }
                else
                {
                    button.addListener(new ActorGestureListener()
                    {
                        @Override
                        public void tap(InputEvent event, float x, float y, int count, int button)
                        {
                            if (count == 2)
                            {
                                JSONObject args = new JSONObject();
                                args.put("content", item.getID());

                                BrainOutClient.SocialController.sendRequest("offline_force_unlock", args,
                                        new SocialController.RequestCallback()
                                        {
                                            @Override
                                            public void success(JSONObject response)
                                            {
                                                PlayerSelectionMenu.this.reset();
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

            if (slot.hasTags())
            {
                Button favoriteButton = new Button(BrainOutClient.Skin, "button-hoverable-clear");

                Image img = new Image(BrainOutClient.Skin,
                        userProfile.isFavorite(item.getID()) ? "icon-favorite-on" : "icon-favorite-off");

                img.setScaling(Scaling.none);
                img.setTouchable(Touchable.disabled);
                img.setFillParent(true);
                favoriteButton.addActor(img);

                favoriteButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        if (userProfile.isFavorite(item.getID()))
                        {
                            img.setDrawable(BrainOutClient.Skin, "icon-favorite-off");
                            setFavorite(userProfile, item, false);
                        }
                        else
                        {
                            img.setDrawable(BrainOutClient.Skin, "icon-favorite-on");
                            setFavorite(userProfile, item, true);
                        }
                    }
                });

                favoriteButton.setSize(24, 24);

                favoriteButton.setVisible(false);

                corners.setCorner(CornerButtons.Corner.topLeft, favoriteButton, 24, 24);

                slotItemContainer.addListener(new InputListener()
                {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                    {
                        super.enter(event, x, y, pointer, fromActor);

                        favoriteButton.setVisible(true);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                    {
                        super.exit(event, x, y, pointer, toActor);

                        favoriteButton.setVisible(false);
                    }
                });

                // setFavorite
            }

            if (
                ((slot.getID().equals("slot-melee") || slot.getID().equals("slot-player-skin") || slot.getID().equals("slot-store"))
                    && (item.hasItem(userProfile, true) || !item.isLocked(userProfile)))
            )
            {
                if (item.getDescription().isValid())
                {
                    Tooltip.RegisterStandardToolTip(button, item.getTitle().get(), item.getDescription().get(), this);
                }
                else
                {
                    Tooltip.RegisterToolTip(button, item.getTitle().get(), this);
                }
            }
            else
            {
                ContentProgressComponent cpc = item.getComponent(ContentProgressComponent.class);

                if (cpc != null)
                {
                    if (cpc.isComplete(userProfile))
                    {
                        Tooltip.RegisterToolTip(button, item.getTitle().get(), this);
                    }
                    else
                    {
                        Tooltip.RegisterToolTip(button, new ContentProgressTooltipCreator(item, userProfile), this);
                    }
                }
                else
                {
                    if (item instanceof InstrumentSlotItem && ((InstrumentSlotItem) item).getInstrument() instanceof Weapon &&
                            ((Weapon) ((InstrumentSlotItem) item).getInstrument()).isNoWeaponStats())
                    {
                        Tooltip.RegisterToolTip(button, item.getTitle().get(), this);
                    }
                    else
                    {
                        UnlockTooltip.show(button, item, BrainOutClient.ClientController.getUserProfile(), this);
                    }
                }

            }

            corners.init();

            items.add(slotItemContainer).size(204, 64).pad(2);

            if (++i >= 3)
            {
                i = 0;
                items.row();
            }

            MenuBadge.apply(button, item,
                    item.hasItem(userProfile) ? MenuBadge.Mode.click : MenuBadge.Mode.hover, ITEM_BADGE);
        }

        ScrollPane scrollPane = new ScrollPane(items, BrainOutClient.Skin, "scroll-default");
        tab.setUserObject(scrollPane);
        scrollPane.setFadeScrollBars(false);

        content.add(scrollPane).padTop(6).padBottom(6).expandX().fillX().row();

        if (isExchangeSlot(slot))
        {
            tab.add(content).maxHeight(444).expandX().fillX().row();
        }
        else
        {
            tab.add(content).minHeight(444).maxHeight(444).expandX().fillX().row();
        }
    }

    private boolean lockedItemClicked(SlotItem item)
    {
        if (!item.hasTag("unique"))
            return false;

        ContentCard contentCard = BrainOutClient.ContentMgr.queryOneContentTpl(ContentCard.class,
            check ->
        {
            ContentCardComponent cmp = check.getComponent(ContentCardComponent.class);

            if (cmp == null)
                return false;

            return cmp.getOwnableContent() == item;
        });

        if (contentCard == null)
        {
            return false;
        }

        Case case_ = BrainOutClient.ContentMgr.queryOneContentTpl(Case.class,
            check ->
        {
            for (Case.Card card : check.getCards())
            {
                if (card.getGroups().contains(contentCard.getGroup(), true))
                {
                    return true;
                }
            }

            return false;
        });

        if (case_ == null)
        {
            return false;
        }

        if (BrainOutClient.Env.storeEnabled())
        {
            pushMenu(new StoreMenu(case_));
        } else
        {
            if (Gdx.app.getNet().openURI("https://brainout.org/store"))
            {
                Gdx.app.postRunnable(() ->
                    BrainOutClient.getInstance().topState().pushMenu(
                        new AlertPopup(L.get("MENU_BROWSER_TAB"))));
            }
        }

        return true;
    }

    private void selectItem(Slot slot, SlotItem.Selection selection)
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        String key = (selectedLayout != null && !selectedLayout.getKey().isEmpty())
            ? "-" + selectedLayout.getKey() : "";

        userProfile.setSelection(slot.getID() + key, selection.getItem().getID());

        if (selectedLayout != null)
        {
            shopCart.setLayout(selectedLayout.getID());
        }

        shopCart.selectItem(slot, selection);
    }

    private void purchaseItem(SlotItem item)
    {
        BrainOutClient.ClientController.sendTCP(new ContentActionMsg(item, ContentActionMsg.Action.purchase));
    }

    public UserPanel getUserPanel()
    {
        return userPanel;
    }

    private void updatePlayerInfo()
    {
        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
        if (csGame != null)
        {
            playerInfo.clear();

            switch (userPanelMode)
            {
                case tech:
                {
                    this.userPanel = BrainOutClient.Env.createTechUserPanel();
                    this.userPanel.setOpenExchange(this::openExchange);
                    break;
                }
                case standard:
                default:
                {
                    this.userPanel = BrainOutClient.Env.createUserPanel(true);
                    this.userPanel.setOpenExchange(this::openExchange);
                    break;
                }
            }

            playerInfo.add(userPanel).expandX().fillX().row();

        }
    }

    private void openExchange()
    {
        Slot storeSlot = BrainOutClient.ContentMgr.get(Constants.User.STORE_SLOT, Slot.class);

        if (storeSlot == null)
            return;

        if (selectedSlot != null)
            closeSlotInfo();

        setTechUserPanel();

        renderSlot(storeSlot);

        selectedSlot = storeSlot;
    }

    private void setStandardUserPanel()
    {
        userPanelMode = UserPanelMode.standard;
        updatePlayerInfo();
    }

    private void setTechUserPanel()
    {
        userPanelMode = UserPanelMode.tech;
        updatePlayerInfo();
    }

    private void updateEvensInfo()
    {
        eventsContent.clear();

        RenderEvents(eventsContent, "MENU_EVENTS", EventType.daily, this::eventSelected);
        RenderEvents(eventsContent, "MAP_SHOOTING_RANGE", EventType.shootingRange, this::eventSelected);
        RenderEvents(eventsContent, "MENU_VALUABLES", EventType.valuables, this::eventSelected);

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        if (userProfile != null && userProfile.isParticipatingClan())
        {
            RenderEvents(eventsContent, "MENU_TOURNAMENTS", EventType.group, this::eventSelected);
        }
    }

    public interface EventSelectedCallback
    {
        void selected(ClientEvent event, EventType eventType);
    }

    public static void RenderEvents(Table eventsContent, String title, EventType eventType,
        EventSelectedCallback eventSelected)
    {
        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);
        if (csGame == null || csGame.isSpectator())
        {
            return;
        }

        Array<ClientEvent> events = BrainOutClient.ClientController.getOnlineEvents();

        boolean valid = false;

        for (ClientEvent event : events)
        {
            if (IsEventInvalid(event, eventType))
                continue;

            if (event.getEvent().isValid())
            {
                valid = true;
                break;
            }
        }

        if (!valid)
            return;


        if (title != null)
        {
            String textStyle;

            switch (eventType)
            {
                case shootingRange:
                case valuables:
                {
                    textStyle = "title-small";
                    break;
                }

                default:
                {
                    textStyle = "title-level";
                    break;
                }
            }

            Label titleText = new Label(L.get(title), BrainOutClient.Skin, textStyle);

            titleText.setAlignment(Align.center);

            String titleStyle;

            switch (eventType)
            {
                case shootingRange:
                case valuables:
                {
                    titleStyle = "form-yellow";
                    break;
                }

                default:
                {
                    titleStyle = "form-red";
                    break;
                }
            }

            eventsContent.add(new BorderActor(titleText, titleStyle)).size(192, 32).expandX().fillX().row();
        }

        for (ClientEvent event : events)
        {
            if (IsEventInvalid(event, eventType))
                continue;

            if (!event.getEvent().isValid())
                continue;

            String buttonStyle;

            switch (eventType)
            {
                case shootingRange:
                case valuables:
                {
                    buttonStyle = title != null ? "button-yellow" : "button-notext";
                    break;
                }

                default:
                {
                    buttonStyle = "button-red";
                    break;
                }
            }

            Button button = new Button(BrainOutClient.Skin, buttonStyle);

            String iconId = event.getIconId();
            Weapon asWeapon = BrainOutClient.ContentMgr.get(iconId, Weapon.class);

            if (asWeapon != null)
            {
                InstrumentInfo info = new InstrumentInfo();

                info.instrument = asWeapon;
                info.skin = asWeapon.getDefaultSkin();

                float scale;

                InstrumentAnimationComponent iac =
                    asWeapon.getComponentFrom(InstrumentAnimationComponent.class);

                if (iac != null)
                {
                    scale = iac.getIconScale();
                }
                else
                {
                    scale = 1.0f;
                }

                InstrumentIcon instrumentIcon = new InstrumentIcon(info, scale, false);

                instrumentIcon.setFillParent(true);
                instrumentIcon.setTouchable(Touchable.disabled);
                instrumentIcon.setBounds(0, 0, 192, 64);
                instrumentIcon.init();

                button.add(instrumentIcon);

            }
            else
            {
                TextureRegion iconRegion = event.getIcon();

                if (iconRegion != null)
                {
                    Image iconImage = new Image(iconRegion);
                    iconImage.setFillParent(true);
                    iconImage.setTouchable(Touchable.disabled);
                    iconImage.setScaling(Scaling.none);

                    button.addActor(iconImage);
                }
            }

            button.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent inputEvent, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    eventSelected.selected(event, eventType);
                }
            });

            switch (event.getEvent().behaviour)
            {
                case increment:
                {
                    if (event.getEvent().getRewardsCount() > 0)
                    {
                        String amount = String.valueOf(event.getEvent().getRewardsUnlocked()) + " / " + event.getEvent().getRewardsCount();

                        Table amountData = new Table();
                        amountData.align(Align.right | Align.bottom);
                        amountData.setFillParent(true);

                        Label amountTitle = new Label(amount, BrainOutClient.Skin, "title-small");
                        amountTitle.setTouchable(Touchable.disabled);
                        amountData.add(amountTitle).pad(4);

                        button.addActor(amountData);
                    }

                    break;
                }
                case maximum:
                {
                    Table amountData = new Table();
                    amountData.align(Align.right | Align.bottom);
                    amountData.setFillParent(true);

                    Label amountTitle = new Label(String.valueOf((int)event.getEvent().score),
                        BrainOutClient.Skin, "title-small");

                    amountTitle.setTouchable(Touchable.disabled);
                    amountData.add(amountTitle).pad(4);

                    button.addActor(amountData);

                    break;
                }
            }

            MenuBadge.apply(button, event.getEvent(), MenuBadge.Mode.click);

            eventsContent.add(button).size(192, 64).expandX().fillX().row();
        }

        eventsContent.add().row();
    }

    private static boolean IsEventInvalid(ClientEvent event, EventType eventType)
    {
        switch (eventType)
        {
            case group:
            {
                if (!event.getEvent().group)
                    return true;

                break;
            }
            case daily:
            {
                if (event.getEvent().group)
                    return true;

                if (event.getEvent().category.equals("battle"))
                    return true;

                if (event.getEvent().taskAction.equals(Constants.Other.VALUABLES_ACTION))
                    return true;

                if (event.getEvent().taskAction.equals(Constants.Other.SHOOTING_RANGE_ACTION))
                    return true;

                break;
            }
            case valuables:
            {
                if (event.getEvent().group)
                    return true;

                if (event.getEvent().category.equals("battle"))
                    return true;

                if (!event.getEvent().taskAction.equals(Constants.Other.VALUABLES_ACTION))
                    return true;

                break;
            }
            case shootingRange:
            {
                if (event.getEvent().group)
                    return true;

                if (event.getEvent().category.equals("battle"))
                    return true;

                if (event.getEvent().taskAction.equals(Constants.Other.VALUABLES_ACTION))
                    return true;

                if (!event.getEvent().taskAction.equals(Constants.Other.SHOOTING_RANGE_ACTION))
                    return true;

                if (!BrainOutClient.ClientController.isLobby())
                    return true;

                break;
            }
        }

        return false;
    }

    protected void notReady()
    {
        //
    }

    protected void eventSelected(ClientEvent event, EventType eventType)
    {
        notReady();

        pushMenu(new OnlineEventMenu(event, eventType == EventType.valuables));
    }

    public Shop getShop()
    {
        return shop;
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.csGame, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.gameController, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.onlineEventsUpdated, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.notify, this);
        BrainOut.EventMgr.unsubscribeAll(Event.ID.badgeRead);
    }

    protected boolean shouldOpenSlotAtStart()
    {
        GameMode gameMode = BrainOutClient.ClientController.getGameMode();

        if (gameMode == null)
        {
            return true;
        }

        if (!(gameMode.getRealization() instanceof ClientGameRealization))
        {
            return true;
        }

        return ((ClientGameRealization) gameMode.getRealization()).autoOpenShopOnSpawn();
    }

    @Override
    public void onInit()
    {
        super.onInit();

        initPostEffects();

        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
        BrainOut.EventMgr.subscribe(Event.ID.csGame, this);
        BrainOut.EventMgr.subscribe(Event.ID.gameController, this);
        BrainOut.EventMgr.subscribe(Event.ID.onlineEventsUpdated, this);
        BrainOut.EventMgr.subscribe(Event.ID.notify, this);
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
                    case playerInfoUpdated:
                    {
                        updatePlayerInfo();
                        break;
                    }
                    case clanInfoUdated:
                    {
                        updatePlayerClanInfo();
                        break;
                    }
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

            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case openConsole:
                    {
                        pushMenu(new ConsoleMenu());

                        return true;
                    }
                }

                break;
            }
            case onlineEventsUpdated:
            {
                updateEvensInfo();

                break;
            }
        }
        return false;
    }

    protected void userProfileUpdated()
    {
        userPanel.refresh();

        renderTrophies();

        if (selectedSlot != null)
        {
            renderSlot(selectedSlot);
        }

        refreshSlotInfo();

        checkUserProfile();
        updatePlayerClanInfo();
    }

    private void checkUserProfile()
    {
        if (!BrainOut.OnlineEnabled())
            return;

        Timer.schedule(new Timer.Task()
        {
            @Override
            public void run()
            {
                UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

                if (userProfile != null)
                {
                    if (userProfile.getName().isEmpty())
                    {
                        pushMenu(new OKInputPopup(L.get("MENU_ENTER_USERNAME"), "")
                        {
                            @Override
                            public void ok()
                            {
                                if (BrainOutClient.Env.getGameUser().validateName(getValue()))
                                {
                                    BrainOutClient.ClientController.sendTCP(new ChangeNameMsg(getValue()));
                                }
                                else
                                {
                                    checkUserProfile();
                                }
                            }
                        });
                    }
                }
            }
        }, 0.5f);
    }

    @Override
    public void onFocusIn()
    {
        super.onFocusIn();

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }

        for (InstrumentIcon icon : activeIcons)
        {
            icon.resetSkin();
        }

        if (userPanel != null)
        {
            userPanel.refresh();
        }

        updateSlotButtons();

        if (selectedSlot != null)
        {
            renderSlot(selectedSlot);
        }

        if (selectedSlot != null)
        {
            setPostEffect();
        }

        Gdx.input.setCursorCatched(false);

        updateEvensInfo();
        updatePlayerClanInfo();
    }

    @Override
    public boolean escape()
    {
        pushMenu(new ExitMenu());
        return true;
    }

    @Override
    public boolean keyDown(int keycode)
    {
        KeyProperties.Keys action = BrainOutClient.ClientSett.getControls().getKey(keycode);

        if (action != null)
        {
            switch (action)
            {
                case slotPrimary:
                {
                    selectLayout(BrainOutClient.ContentMgr.get("layout-1", Layout.class));

                    break;
                }
                case slotSecondary:
                {
                    selectLayout(BrainOutClient.ContentMgr.get("layout-2", Layout.class));

                    break;
                }
                case slotSpecial:
                {
                    selectLayout(BrainOutClient.ContentMgr.get("layout-3", Layout.class));

                    break;
                }
                case slotKnife:
                {
                    selectLayout(BrainOutClient.ContentMgr.get("layout-4", Layout.class));

                    break;
                }
                case playerList:
                {
                    if (BrainOutClient.ClientController.canSeePlayerList())
                    {
                        pushMenu(new PlayerListMenu());
                    }

                    return true;
                }
                case console:
                {
                    Gdx.app.postRunnable(() -> pushMenu(new ConsoleMenu()));

                    return true;
                }
            }
        }

        return super.keyDown(keycode);
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
        else if (event.notifyAward == NotifyAward.ownable &&
                event.method == NotifyMethod.fix &&
                event.reason == NotifyReason.contentRepaired)
        {
            NotifyData notifyData = event.data;

            if (notifyData instanceof ContentND)
            {
                ContentND contentND = ((ContentND) notifyData);

                Content content = BrainOut.ContentMgr.get(contentND.id);

                repairedContent(content);
            }
        }
    }

    private void repairedContent(Content content)
    {
        playSound(MenuSound.repair);
        repaired = content;
        refreshSlotInfo();
    }

    private void gotOwnable(OwnableContent ownableContent)
    {
        if (ownableContent instanceof SlotItem)
        {
            SlotItem toInstall = ((SlotItem) ownableContent);

            selectedSlot = null;
            selectItem(toInstall.getSlot(), toInstall.getStaticSelection());
            save();

            playSound(MenuSound.equip);

            reset();
        }
    }

    public OpenWindowMode getOpenWindowMode()
    {
        return openWindowMode;
    }

    public void setOpenWindowMode(OpenWindowMode openWindowMode)
    {
        if (this.openWindowMode == openWindowMode)
            return;

        cleanUpWindowMode();

        this.openWindowMode = openWindowMode;
    }

    protected void cleanUpWindowMode()
    {
        if (openWindowMode == OpenWindowMode.slot)
        {
            if (slotButtonsGroup.getChecked() != null)
            {
                slotButtonsGroup.getChecked().setChecked(false);
            }

            selectedSlot = null;
        }
    }

    protected Table getMainContent()
    {
        return mainContent;
    }
}
