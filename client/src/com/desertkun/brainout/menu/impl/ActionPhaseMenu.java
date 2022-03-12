package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientConstants;
import com.desertkun.brainout.L;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.settings.ClientSettings;
import com.desertkun.brainout.client.settings.KeyProperties;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.components.ClientWeaponSlotComponent;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.WeaponSlotComponent;
import com.desertkun.brainout.components.my.MyPlaceComponent;
import com.desertkun.brainout.components.my.MyPlayerComponent;
import com.desertkun.brainout.components.my.MyWeaponComponent;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.block.Block;
import com.desertkun.brainout.content.components.IconComponent;
import com.desertkun.brainout.content.components.InstrumentAnimationComponent;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.FreePlayMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.PlaceBlockData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.gs.actions.WaitAction;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.RichAlertPopup;
import com.desertkun.brainout.menu.ui.BorderActor;
import com.desertkun.brainout.menu.ui.InstrumentIcon;
import com.desertkun.brainout.menu.ui.Minimap;
import com.desertkun.brainout.menu.ui.Notifications;
import com.desertkun.brainout.menu.widgets.ProfilingWidget;
import com.desertkun.brainout.menu.widgets.chat.InGameChatWidget;
import com.desertkun.brainout.mode.ClientRealization;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.playstate.PlayStateGame;

import java.nio.IntBuffer;

public class ActionPhaseMenu extends Menu implements EventReceiver
{
    private PlayerData myPlayerData;
    private VerticalGroup killList;
    private ScrollPane killPane;
    private Table playerInfo;
    private Runnable escapeOverride;
    private Minimap minimap;
    private BorderActor instrumentIcon;
    private InstrumentData lastPlayerInstrument;
    private Label weaponInfo;
    private Label activation;
    private ClientActiveActivatorComponentData currentActivator;
    private boolean currentActivatorState;
    private int previousWeaponState;
    private float activatorCheck;
    private float updatePlayerInfoCounter;
    private boolean isRenderBlocked;

    /*
    private Box2DDebugRenderer debugRenderer;
    */

    public ActionPhaseMenu()
    {
    }

    @Override
    public Table createUI()
    {
        return null;
    }

    public boolean isMinimapEnabled()
    {
        return BrainOutClient.PackageMgr.getDefine("minimapEnabled", "true").equals("true");
    }

    public void initWidgets()
    {
        if (isMinimapEnabled() && ClientSettings.IsFBOSupported())
        {
            minimap = new Minimap();
            minimap.setSize(256, 128);
            minimap.setPosition(16, getHeight() - 144);
            addActor(minimap);
        }

        playerInfo = new Table();
        playerInfo.align(Align.bottom);

        playerInfo.setBounds(
                BrainOutClient.getWidth() - ClientConstants.Menu.PlayerInfo.X - ClientConstants.Menu.PlayerInfo.WIDTH,
                ClientConstants.Menu.PlayerInfo.Y,
                ClientConstants.Menu.PlayerInfo.WIDTH,
                ClientConstants.Menu.PlayerInfo.HEIGHT
        );
        playerInfo.align(Align.right | Align.bottom);

        addActor(playerInfo);

        activation = new Label("", BrainOutClient.Skin, "title-small");
        activation.setAlignment(Align.center);
        activation.setBounds(
            BrainOutClient.getWidth() / 2.0f - 256,
            128,
            512, 32
        );

        activation.setVisible(false);

        addActor(activation);

        killList = new VerticalGroup();
        killList.align(Align.right);

        killPane = new ScrollPane(killList, BrainOutClient.Skin, "scroll-default");
        killPane.setBounds(
            BrainOutClient.getWidth() - ClientConstants.Menu.KillList.X - ClientConstants.Menu.KillList.WIDTH,
            BrainOutClient.getHeight() - ClientConstants.Menu.KillList.Y - ClientConstants.Menu.KillList.HEIGHT,
            ClientConstants.Menu.KillList.WIDTH,
            ClientConstants.Menu.KillList.HEIGHT);

        addActor(killPane);

        GameState gs = getGameState();

        if (gs != null)
        {
            gs.getWidgets().removeAll();

            InGameChatWidget chat = new InGameChatWidget(
                    ClientConstants.Menu.Chat.OFFSET_X,
                    ClientConstants.Menu.Chat.OFFSET_Y,
                    ClientConstants.Menu.Chat.WIDTH,
                    ClientConstants.Menu.Chat.HEIGHT);

            gs.getWidgets().addWidget(chat);

            ProfilingWidget profiling = new ProfilingWidget(
                    BrainOutClient.getWidth() - 200, 200, 200, 512);

            gs.getWidgets().addWidget(profiling);
        }

        isRenderBlocked = false;
    }

    @Override
    public void onInit()
    {
        BrainOut.EventMgr.subscribe(Event.ID.setMyPlayer, this);
        BrainOut.EventMgr.subscribe(Event.ID.kill, this);
        BrainOut.EventMgr.subscribe(Event.ID.simple, this);
        BrainOut.EventMgr.subscribe(Event.ID.onScreenMessage, this);
        BrainOut.EventMgr.subscribe(Event.ID.notify, this);
        BrainOut.EventMgr.subscribe(Event.ID.weaponStateUpdated, this);
        BrainOut.EventMgr.subscribe(Event.ID.gameController, this);
        BrainOut.EventMgr.subscribe(Event.ID.selectSlot, this);
        BrainOut.EventMgr.subscribe(Event.ID.selectPreviousSlot, this);

        initWidgets();

        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);

        if (csGame != null)
        {
            myPlayerData = csGame.getPlayerData();
        }

        updatePlayerInfo();

        initPlayStateWidgets();
    }

    private void initPlayStateWidgets()
    {
        PlayStateGame game = ((PlayStateGame) BrainOutClient.ClientController.getPlayState());
        ((ClientRealization) game.getMode().getRealization()).init(this);
    }

    public void updatePlayerInfo()
    {
        try
        {
            playerInfo.clear();

            if ("disabled".equals(BrainOutClient.PackageMgr.getDefine("weaponStats", "on")))
            {
                return;
            }

            playerInfo.setVisible(isAtFocus());

            if (myPlayerData != null && myPlayerData.isAlive() && !myPlayerData.isWounded())
            {
                PlayerBoostersComponentData bst = myPlayerData.getComponent(PlayerBoostersComponentData.class);

                Table boosters = new Table();

                if (bst != null)
                {
                    renderBoosters(boosters, bst);
                }

                {
                    PlayerOwnerComponent poc = myPlayerData.getComponent(PlayerOwnerComponent.class);

                    if (poc != null)
                    {

                        if (poc.getConsumableContainer().getWeight() > myPlayerData.getMaxOverweight())
                        {
                            generateBooster(boosters, "icon-boost-weight");
                        }
                    }
                }

                playerInfo.add(boosters).right().padBottom(8).row();

                Table mainInfo = new Table();
                playerInfo.add(mainInfo).right().row();

                {
                    InstrumentData currentInstrument = myPlayerData.getCurrentInstrument();

                    if (currentInstrument != null && currentInstrument.getInfo().skin != null)
                    {
                        IconComponent iconComponent = currentInstrument.getInstrument().getComponent(IconComponent.class);
                        TextureRegion icon = null;

                        if (iconComponent == null)
                        {
                            iconComponent = currentInstrument.getInfo().skin.getComponent(IconComponent.class);
                        }

                        if (iconComponent != null)
                        {
                            icon = iconComponent.getIcon("big-icon", null);
                        }
                        else
                        {
                            MyWeaponComponent mwc = currentInstrument.getComponent(MyWeaponComponent.class);

                            if (mwc != null)
                            {
                                WeaponSlotComponent currentSlot = mwc.getCurrentSlot();
                                if (currentSlot instanceof ClientWeaponSlotComponent)
                                {
                                    String iconName = ((ClientWeaponSlotComponent) currentSlot).getIcon();

                                    if (iconName != null)
                                    {
                                        icon = BrainOutClient.getRegion(iconName);
                                    }
                                }
                            }
                        }

                        if (icon != null)
                        {
                            Table instrumentIcon = new Table();

                            Image image = new Image(icon);
                            image.setScaling(Scaling.none);
                            instrumentIcon.add(image);

                            BorderActor iconBorder = new BorderActor(instrumentIcon);
                            iconBorder.getCell().size(ClientConstants.Menu.PlayerInfo.INSTRUMENT_ICON_WIDTH,
                                    ClientConstants.Menu.PlayerInfo.INSTRUMENT_ICON_HEIGHT);

                            mainInfo.add(iconBorder);
                        }
                        else
                        {
                            GameMode gameMode = BrainOutClient.ClientController.getGameMode();
                            if (gameMode != null && gameMode.getID() == GameMode.ID.free)
                            {
                                MyWeaponComponent mwc = currentInstrument.getComponent(MyWeaponComponent.class);

                                if (mwc != null)
                                {
                                    WeaponSlotComponent currentSlot = mwc.getCurrentSlot();
                                    if (currentSlot instanceof ClientWeaponSlotComponent)
                                    {
                                        if (currentSlot.hasMagazineManagement())
                                        {
                                            Table mags = new Table();

                                            IntMap.Keys keys = currentSlot.getMagazines();
                                            while (keys.hasNext)
                                            {
                                                int id = keys.next();
                                                int rounds = currentSlot.getMagazineStatus(id);

                                                TextureRegion region = BrainOutClient.Skin.getRegion(
                                                    ClientConstants.Menu.FreePlay.GetMagazineImage(
                                                        (float)rounds / currentSlot.getClipSize().asFloat()));

                                                String style;
                                                if (
                                                    currentSlot.getState() == WeaponSlotComponent.State.loadMagazineRound
                                                    && id == currentSlot.getLoadMagazineId())
                                                {
                                                    style = "button-highlighted-magazine";
                                                }
                                                else
                                                    style = "button-inventory";

                                                Button upgrade = new Button(BrainOutClient.Skin, style);
                                                Image magazine = new Image(new TextureRegionDrawable(region));
                                                magazine.setTouchable(Touchable.disabled);
                                                magazine.setFillParent(true);
                                                magazine.setAlign(Align.center);
                                                magazine.setScaling(Scaling.none);
                                                upgrade.addActor(magazine);

                                                mags.add(upgrade).size(48, 64);
                                            }

                                            if (currentSlot.hasMagazineAttached())
                                            {
                                                TextureRegion region = BrainOutClient.Skin.getRegion(
                                                    ClientConstants.Menu.FreePlay.GetMagazineImage(
                                                        (float)currentSlot.getRounds() /
                                                            currentSlot.getClipSize().asFloat()));

                                                Button upgrade = new Button(BrainOutClient.Skin, "button-selected-magazine");

                                                Image magazine = new Image(new TextureRegionDrawable(region));
                                                magazine.setTouchable(Touchable.disabled);
                                                magazine.setFillParent(true);
                                                magazine.setAlign(Align.center);
                                                magazine.setScaling(Scaling.none);
                                                upgrade.addActor(magazine);

                                                mags.add(upgrade).size(48, 64);
                                            }

                                            mainInfo.add(mags);
                                        }
                                    }
                                }
                            }

                            if (currentInstrument != this.lastPlayerInstrument)
                            {
                                float scale;

                                InstrumentAnimationComponent iac = currentInstrument.getInstrument().
                                        getComponentFrom(InstrumentAnimationComponent.class);

                                if (iac != null)
                                {
                                    scale = iac.getIconScale();
                                }
                                else
                                {
                                    scale = 1.0f;
                                }

                                InstrumentIcon image = new InstrumentIcon(currentInstrument.getInfo(), scale, false);
                                image.setFillParent(true);
                                image.init();

                                this.instrumentIcon = new BorderActor(image);
                                this.instrumentIcon.getCell().size(ClientConstants.Menu.PlayerInfo.INSTRUMENT_ICON_WIDTH,
                                        ClientConstants.Menu.PlayerInfo.INSTRUMENT_ICON_HEIGHT);

                                this.lastPlayerInstrument = currentInstrument;
                            }

                            mainInfo.add(this.instrumentIcon).padRight(8);
                        }
                    }
                }

                Table stats = new Table();

                {
                    HealthComponentData hcd = myPlayerData.getComponent(HealthComponentData.class);

                    Group group = new Group();
                    group.setSize(ClientConstants.Menu.PlayerInfo.LABEL_WIDTH, ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT);

                    String value = String.valueOf(Math.floor(hcd.getHealth()));

                    ProgressBar healthBar = new ProgressBar(0, hcd.getInitHealth(), 1, false, BrainOutClient.Skin,
                            "progress-health");
                    healthBar.setValue(hcd.getHealth());

                    TemperatureComponentData tmp = myPlayerData.getComponent(TemperatureComponentData.class);
                    if (tmp != null && tmp.getFreezing() > 0)
                    {
                        float bound = (tmp.getFreezing() / hcd.getInitHealth()) * ClientConstants.Menu.PlayerInfo.LABEL_WIDTH;

                        Image img = new Image(BrainOutClient.Skin, "progress-knob-freeze");
                        img.setBounds(
                            0,
                            0,
                            bound,
                            ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT
                        );

                        group.addActor(img);

                        healthBar.setBounds(
                            bound,
                            0,
                            ClientConstants.Menu.PlayerInfo.LABEL_WIDTH - bound,
                            ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT
                        );
                    }
                    else
                    {
                        healthBar.setBounds(
                            0,
                            0,
                            ClientConstants.Menu.PlayerInfo.LABEL_WIDTH,
                            ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT
                        );
                    }

                    group.addActor(healthBar);

                    Label playerHealth = new Label(value, BrainOutClient.Skin, "player-health");

                    playerHealth.setAlignment(Align.right);
                    playerHealth.setBounds(
                        ClientConstants.Menu.PlayerInfo.LABEL_OFFSET_X,
                        ClientConstants.Menu.PlayerInfo.LABEL_OFFSET_Y,
                        ClientConstants.Menu.PlayerInfo.LABEL_WIDTH,
                        ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT
                    );

                    group.addActor(playerHealth);

                    BorderActor groupBorder = new BorderActor(group);
                    groupBorder.getCell().padRight(2).padLeft(2);
                    stats.add(groupBorder).row();
                }

                InstrumentData currentInstrument = myPlayerData.getCurrentInstrument();

                if (currentInstrument != null)
                {
                    Group group = new Group();
                    group.setSize(ClientConstants.Menu.PlayerInfo.LABEL_WIDTH, ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT);

                    Table groupContent = new Table();

                    if (currentInstrument instanceof WeaponData)
                    {
                        WeaponData weaponData = ((WeaponData) currentInstrument);
                        MyWeaponComponent mwc = weaponData.getComponent(MyWeaponComponent.class);

                        if (mwc != null)
                        {
                            WeaponSlotComponent slot = mwc.getCurrentSlot();
                            Weapon.WeaponProperties properties = slot.getWeaponProperties();

                            if (!properties.isUnlimited())
                            {

                                TextureRegion icon;

                                switch (slot.getShootMode())
                                {
                                    case single:
                                    {
                                        icon = BrainOutClient.getRegion("shootmode-single");
                                        break;
                                    }
                                    case singleCock:
                                    {
                                        icon = BrainOutClient.getRegion("shootmode-single");
                                        break;
                                    }
                                    case couple:
                                    {
                                        icon = BrainOutClient.getRegion("shootmode-couple");
                                        break;
                                    }
                                    case burst:
                                    {
                                        icon = BrainOutClient.getRegion("shootmode-burst");
                                        break;
                                    }
                                    case burst2:
                                    {
                                        icon = BrainOutClient.getRegion("shootmode-couple");
                                        break;
                                    }
                                    case auto:
                                    default:
                                    {
                                        icon = BrainOutClient.getRegion("shootmode-auto");
                                        break;
                                    }
                                }

                                groupContent.add(new Image(icon));

                                PlayerOwnerComponent poc = myPlayerData.getComponent(PlayerOwnerComponent.class);

                                if (poc != null)
                                {
                                    previousWeaponState = 0;

                                    this.weaponInfo = new Label("", BrainOutClient.Skin, "title-bullets");
                                    weaponInfo.setAlignment(Align.right);

                                    updateWeaponInfo(weaponData, slot, slot.getState());

                                    groupContent.add(weaponInfo).expand().fill().right();
                                }
                            }
                        }
                    }
                    else if (currentInstrument instanceof PlaceBlockData)
                    {
                        MyPlaceComponent mpc = currentInstrument.getComponent(MyPlaceComponent.class);

                        if (mpc != null)
                        {
                            Block current = mpc.getCurrentBlock();

                            if (current != null)
                            {
                                if (current.hasComponent(IconComponent.class))
                                {
                                    IconComponent iconComponent = current.getComponent(IconComponent.class);

                                    groupContent.add(new Image(iconComponent.getIcon()));
                                }

                                Label blockName = new Label(current.getTitle().get(), BrainOutClient.Skin, "title-small");
                                groupContent.add(blockName).padLeft(8).padRight(4);

                                PlayerOwnerComponent poc = myPlayerData.getComponent(PlayerOwnerComponent.class);

                                if (poc != null)
                                {
                                    ConsumableRecord record = poc.getConsumableContainer().getConsumable(current);

                                    if (record != null)
                                    {
                                        String value = Integer.toString(record.getAmount());

                                        Label bulletsInfo = new Label(value, BrainOutClient.Skin, "title-bullets");

                                        bulletsInfo.setAlignment(Align.right);

                                        groupContent.add(bulletsInfo).expand().fill().right();
                                    }
                                }
                            }
                            else
                            {
                                Label bulletsInfo = new Label(L.get("MENU_NO_BLOCKS"),
                                        BrainOutClient.Skin, "title-small");

                                bulletsInfo.setAlignment(Align.center);

                                groupContent.add(bulletsInfo).expand().fill().right();
                            }
                        }
                    }
                    else
                    {
                        String value = currentInstrument.getInstrument().getTitle().get();
                        Label instrumentInfo = new Label(value, BrainOutClient.Skin, "title-weapon");
                        instrumentInfo.setAlignment(Align.center);

                        groupContent.add(instrumentInfo).expand().fill().right();
                    }

                    groupContent.setFillParent(true);
                    group.addActor(groupContent);

                    BorderActor groupBorder = new BorderActor(group);
                    groupBorder.getCell().padRight(2).padLeft(2);
                    stats.add(groupBorder).row();
                }

                mainInfo.add(stats);
            }
        }
        catch (GdxRuntimeException e)
        {
            // i don't have time for this
        }
    }

    private void checkBooster(String id, String icon, Table boosters, PlayerBoostersComponentData bst)
    {
        PlayerBoostersComponentData.Booster b = bst.getBooster(id);

        if (b != null)
            generateBooster(boosters, icon);
    }

    private void renderBoosters(Table boosters, PlayerBoostersComponentData bst)
    {
        checkBooster("speed", "icon-boost-speed", boosters, bst);
        checkBooster("radx", "icon-boost-radx", boosters, bst);

        FreeplayPlayerComponentData fp = myPlayerData.getComponent(FreeplayPlayerComponentData.class);

        if (fp != null)
        {
            if (fp.isHungry())
            {
                generateBooster(boosters, "icon-boost-hunger");
            }

            if (fp.isThirsty())
            {
                generateBooster(boosters, "icon-boost-thirst");
            }

            if (fp.isCold())
            {
                generateBooster(boosters, "icon-boost-cold", 1.0f - fp.getTemperatureCoef());
            }

            if (fp.hasBonesBroken())
            {
                generateBooster(boosters, "icon-boost-bones");
            }

            if (fp.isBleeding())
            {
                generateBooster(boosters, "icon-boost-bleeding");
            }

            if (fp.hasRadioDanger())
            {
                generateBooster(boosters, "icon-radiation-max");
            }
            else if (fp.hasRadioWarning())
            {
                generateBooster(boosters, "icon-radiation-mid");
            }
        }
    }

    private void generateBooster(Table boosters, String icon)
    {
        Image image = new Image(BrainOutClient.Skin, icon);
        image.setScaling(Scaling.none);
        boosters.add(image).size(32, 32);
    }

    private void generateBooster(Table boosters, String icon, float alpha)
    {
        Image image = new Image(BrainOutClient.Skin, icon);
        image.setScaling(Scaling.none);
        image.setColor(1, 1, 1, alpha);
        boosters.add(image).size(32, 32);
    }

    private void updateWeaponInfo(WeaponData weaponData, WeaponSlotComponent slot,
                                  WeaponSlotComponent.State state)
    {
        if (myPlayerData == null)
            return;

        if (weaponInfo == null)
            return;

        PlayerOwnerComponent poc = myPlayerData.getComponent(PlayerOwnerComponent.class);

        if (poc == null)
            return;

        int stateId;

        switch (state)
        {
            case fetchWait:
            {
                stateId = ClientConstants.WeaponState.PULL;

                break;
            }
            case misfireWait:
            {
                stateId = ClientConstants.WeaponState.MISFIRE;

                break;
            }
            case fetching:
            {
                stateId = ClientConstants.WeaponState.PULLING;

                break;
            }
            case cocking:
            {
                stateId = ClientConstants.WeaponState.COCKING;

                break;
            }
            case cocked:
            {
                stateId = ClientConstants.WeaponState.COCKED;

                break;
            }
            case reloading:
            case reloadingBoth:
            {
                stateId = ClientConstants.WeaponState.RELOADING;

                break;
            }
            case loadMagazineRound:
            {
                stateId = ClientConstants.WeaponState.ADDING_ROUNDS;
                break;
            }
            case stuck:
            case stuckIdle:
            {
                stateId = ClientConstants.WeaponState.STUCK;

                break;
            }
            case empty:
            {
                stateId = ClientConstants.WeaponState.EMPTY;
                break;
            }

            default:
            {
                if (slot.getWeaponProperties().hasChambering() && slot.getChambered() == 0 && slot.getRounds() > 0)
                {
                    stateId = ClientConstants.WeaponState.PULL;
                }
                else
                {
                    stateId = ClientConstants.WeaponState.NORMAL;
                }

                break;
            }
        }

        if (slot.isDetached())
        {
            stateId = ClientConstants.WeaponState.DETACHED;
        }

        if (previousWeaponState != stateId)
        {
            previousWeaponState = stateId;

            String value;
            boolean notice;
            boolean hard = false;

            switch (stateId)
            {
                case ClientConstants.WeaponState.EMPTY:
                {
                    value = L.get("MENU_WEAPON_UI_EMPTY");
                    notice = true;

                    break;
                }
                case ClientConstants.WeaponState.STUCK:
                {
                    value = L.get("MENU_WEAPON_UI_STUCK");
                    notice = true;

                    break;
                }
                case ClientConstants.WeaponState.MISFIRE:
                {
                    value = L.get("MENU_WEAPON_UI_MISFIRE");
                    notice = true;
                    hard = true;

                    break;
                }
                case ClientConstants.WeaponState.PULL:
                {
                    value = L.get("MENU_WEAPON_UI_PULL");
                    notice = true;

                    break;
                }
                case ClientConstants.WeaponState.PULLING:
                {
                    value = L.get("MENU_WEAPON_UI_PULLING");
                    notice = true;

                    break;
                }
                case ClientConstants.WeaponState.COCKING:
                {
                    value = L.get("MENU_WEAPON_UI_COCKING");
                    notice = true;

                    break;
                }
                case ClientConstants.WeaponState.COCKED:
                {
                    value = L.get("MENU_WEAPON_UI_COCKED");
                    notice = true;

                    break;
                }
                case ClientConstants.WeaponState.RELOADING:
                {
                    value = L.get("MENU_WEAPON_UI_RELOADING");
                    notice = true;

                    break;
                }
                case ClientConstants.WeaponState.DETACHED:
                {
                    ConsumableRecord record = poc.getConsumableContainer().getConsumable(
                            slot.getBullet());

                    int amount = record != null ? record.getAmount() : 0;

                    if (slot.hasMagazineManagement())
                    {
                        IntMap.Keys keys = slot.getMagazines();

                        while (keys.hasNext)
                        {
                            int id = keys.next();
                            amount += slot.getMagazineStatus(id);
                        }
                    }

                    String chambered;

                    if (slot.getChambered() > 0)
                    {
                        chambered = String.valueOf(slot.getChambered());
                    }
                    else
                    {
                        chambered = "-";
                    }

                    value = chambered + "/" + amount;
                    notice = false;

                    break;
                }
                case ClientConstants.WeaponState.NORMAL:
                case ClientConstants.WeaponState.ADDING_ROUNDS:
                default:
                {
                    ConsumableRecord record = poc.getConsumableContainer().getConsumable(
                        slot.getBullet());

                    int amount = record != null ? record.getAmount() : 0;

                    value = Integer.toString(slot.getRounds() + slot.getChambered()) + "/" + amount;
                    notice = false;
                }
            }

            weaponInfo.setText(value);
            weaponInfo.clearActions();
            weaponInfo.setColor(hard ? Color.RED : Color.WHITE);

            if (notice)
            {
                if (hard)
                {
                    weaponInfo.addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                        Actions.delay(0.1f),
                        Actions.parallel(
                            Actions.alpha(0)
                        ),
                        Actions.delay(0.1f),
                        Actions.parallel(
                            Actions.alpha(1)
                        )
                    )));
                }
                else
                {

                    weaponInfo.addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                        Actions.alpha(0.25f, 0.25f),
                        Actions.alpha(1.0f, 0.25f)
                    )));
                }
            }
            else
            {
                weaponInfo.getColor().a = 1;
            }
        }

    }

    @Override
    public void onFocusIn()
    {
        BrainOutClient.Env.getGameController().setControllerMode(GameController.ControllerMode.action);
        BrainOutClient.Env.getGameController().reset();

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(true);
        }

        updatePlayerInfo();
    }

    @Override
    public void onFocusOut(Menu toMenu)
    {
        super.onFocusOut(toMenu);

        BrainOutClient.Env.getGameController().setControllerMode(GameController.ControllerMode.disabled);

        if (ClientConstants.Client.MOUSE_LOCK)
        {
            Gdx.input.setCursorCatched(false);
        }

        Gdx.app.postRunnable(this::updatePlayerInfo);
    }

    private void setPlayerData(PlayerData playerData)
    {
        this.myPlayerData = playerData;

        updateInstrument();
    }

    private void updateInstrument()
    {
        if (myPlayerData != null)
        {
            updatePlayerInfo();
        }
        else
        {
            hideInstrument();
        }
    }

    private void hideInstrument()
    {
        //
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOut.EventMgr.unsubscribe(Event.ID.setMyPlayer, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.kill, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.onScreenMessage, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.notify, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.weaponStateUpdated, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.gameController, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.selectSlot, this);
        BrainOut.EventMgr.unsubscribe(Event.ID.selectPreviousSlot, this);

        disposeMinimap();
    }

    @Override
    public boolean keyDown(int keyCode)
    {
        switch (keyCode)
        {
            case Input.Keys.NUM_9:
            {
                if (Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT))
                {
                    for (ClientMap map : Map.All(ClientMap.class))
                    {
                        if (map.getDimension().equals("swamp"))
                            continue;

                        map.setPhysicsDebugging(!map.isPhysicsDebuggingEnabled());
                    }

                    return true;
                }
            }
        }

        return super.keyDown(keyCode);
    }

    private void disposeMinimap()
    {
        if (minimap != null)
        {
            minimap.dispose();
        }
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
            case onScreenMessage:
            {
                OnScreenMessagesEvent e = ((OnScreenMessagesEvent) event);

                if (e.isTimer)
                    addOnScreenTimerMessage(e.message, e.time, e.doNotForce, e.align, e.style, e.name);
                else
                    addOnScreenMessage(e.message, e.time, e.doNotForce, e.align, e.style, e.name);

                break;
            }
            case gameController:
            {
                GameControllerEvent gcEvent = ((GameControllerEvent) event);

                switch (gcEvent.action)
                {
                    case changeTeam:
                    {
                        final CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);

                        if (BrainOutClient.PackageMgr.getDefine("changeTeam", "true").equals("true"))
                        {
                            changeTeam(csGame);
                        }

                        return true;
                    }

                    case openPlayerList:
                    {
                        if (BrainOutClient.ClientController.canSeePlayerList())
                        {
                            pushMenu(new PlayerListMenu());
                        }
                        else if (BrainOutClient.ClientController.canSeeExchangeMenu())
                        {
                            BrainOutClient.ClientController.openExchangeMenu(myPlayerData);
                        }

                        return true;
                    }

                    case openConsole:
                    {
                        pushMenu(new ConsoleMenu());

                        return true;
                    }

                    case back:
                    {
                        if (escapeOverride != null)
                        {
                            escapeOverride.run();
                            return true;
                        }

                        pushMenu(new ExitMenu());

                        return true;
                    }

                    case reload:
                    {
                        if (myPlayerData != null)
                        {
                            myPlayerData.onEvent(gcEvent);
                            updateInstrument();
                        }

                        return true;
                    }

                    case switchSource:
                    {
                        if (myPlayerData != null)
                        {
                            myPlayerData.onEvent(gcEvent);
                            updateInstrument();
                        }

                        return true;
                    }

                    case switchShootMode:
                    {
                        if (myPlayerData != null)
                        {
                            myPlayerData.onEvent(gcEvent);
                            updateInstrument();
                        }

                        return true;
                    }

                    case hideInterface:
                    {
                        isRenderBlocked = gcEvent.flag;
                        break;
                    }
                }

                return myPlayerData != null && myPlayerData.onEvent(gcEvent);

            }

            case selectPreviousSlot:
            {
                return myPlayerData != null && myPlayerData.onEvent(event);
            }

            case selectSlot:
            {
                return myPlayerData != null && myPlayerData.onEvent(event);
            }

            case simple:
            {
                SimpleEvent simpleEvent = ((SimpleEvent) event);

                if (simpleEvent.getAction() == null)
                    return false;

                switch (simpleEvent.getAction())
                {
                    case instrumentUpdated:
                    {
                        if (myPlayerData != null)
                        {
                            updateInstrument();
                        }

                        return true;
                    }
                    case playerInfoUpdated:
                    {
                        updatePlayerInfo();

                        return false;
                    }
                }

                break;
            }
            case setMyPlayer:
            {
                setPlayerData(((MyPlayerSetEvent) event).playerData);
                updatePlayerInfo();

                return true;
            }

            case weaponStateUpdated:
            {
                WeaponStateUpdatedEvent e = ((WeaponStateUpdatedEvent) event);

                if (myPlayerData == null)
                    return false;

                if (e.playerData == myPlayerData && e.weaponData == myPlayerData.getCurrentInstrument())
                {
                    updateWeaponInfo(e.weaponData, e.slot, e.state);
                }

                return true;
            }
            case kill:
            {
                addKill((KillEvent) event);

                break;
            }
            case notify:
            {
                Notifications.AddNotification((NotifyEvent) event);

                break;
            }
        }

        return false;
    }

    private void changeTeam(CSGame csGame)
    {
        GameMode gameMode = BrainOutClient.ClientController.getGameMode();

        if (gameMode != null && !gameMode.allowTeamChange())
            return;

        pushMenu(new SelectTeamMenu(csGame.getTeams(), new SelectTeamMenu.Select()
        {
            @Override
            public void onSelect(Team item, GameState gs)
            {
            }

            @Override
            public void failed(String reason)
            {
                pushMenu(new RichAlertPopup(L.get("MENU_ATTENTION"), L.get(reason)));
            }
        }));
    }

    private ClientActiveActivatorComponentData getClosestWorkingActivator()
    {
        if (myPlayerData == null)
            return null;

        Map map = myPlayerData.getMap();

        if (map == null)
            return null;

        ActiveData closestActive = map.getClosestActive(16, myPlayerData.getX(), myPlayerData.getY(),
            ActiveData.class, activeData ->
        {
            if (activeData == myPlayerData)
                return false;

            ClientActiveActivatorComponentData aa =
                activeData.getComponentWithSubclass(ClientActiveActivatorComponentData.class);
            if (aa == null)
                return false;

            return aa.test(myPlayerData);
        });

        if (closestActive == null)
            return null;

        return closestActive.getComponentWithSubclass(ClientActiveActivatorComponentData.class);
    }


    private ClientActiveActivatorComponentData getAnyClosestActivator()
    {
        if (myPlayerData == null)
            return null;

        Map map = myPlayerData.getMap();

        if (map == null)
            return null;

        ActiveData closestActive = map.getClosestActive(16, myPlayerData.getX(), myPlayerData.getY(),
            ActiveData.class, activeData ->
        {
            if (activeData == myPlayerData)
                return false;

            return activeData.getComponentWithSubclass(ClientActiveActivatorComponentData.class) != null;
        });

        if (closestActive == null)
            return null;

        return closestActive.getComponentWithSubclass(ClientActiveActivatorComponentData.class);
    }

    private ClientActiveActivatorComponentData getClosestActivator()
    {
        {
            ClientActiveActivatorComponentData a = getClosestWorkingActivator();
            if (a != null)
                return a;
        }
        {
            ClientActiveActivatorComponentData a = getAnyClosestActivator();
            if (a != null)
                return a;
        }

        return null;
    }

    private class OnScreenAction extends WaitAction
    {
        protected final String message;
        protected Container<Label> labelContainer;
        protected Label messageTitle;
        private int align;
        private String style;

        public OnScreenAction(float time, String message)
        {
            this(time, message, Align.center, "title-small", null);
        }

        public OnScreenAction(float time, String message, int align, String style, String name)
        {
            super(time);

            this.message = message;
            this.align = align;
            this.style = style == null ? "title-small" : style;
            setName(name);
        }

        @Override
        public void run()
        {
            this.labelContainer = new Container<>();
            this.messageTitle = new Label(message, BrainOutClient.Skin, style);

            messageTitle.setWrap(true);
            messageTitle.setAlignment(Align.center);

            labelContainer.prefWidth(450);
            labelContainer.setActor(messageTitle);
            labelContainer.setFillParent(true);
            labelContainer.align(align);
            labelContainer.pad(50);

            addActor(labelContainer);
        }

        @Override
        public void done()
        {
            labelContainer.remove();

            super.done();
        }
    }

    private class OnScreenTimerAction extends OnScreenAction
    {
        float timer;

        public OnScreenTimerAction(float time, String message, int align, String style, String name)
        {
            super(time, message, align, style, name);
            timer = 0;
        }

        @Override
        public void run()
        {
            super.run();

            messageTitle.setText(String.format(message, (int)time));
        }

        @Override
        public void update(float dt)
        {
            timer += dt;
            if (timer > 1)
            {
                timer -= 1;
                messageTitle.setText(String.format(message, (int)time));
            }

            super.update(dt);
        }
    }

    private void addOnScreenMessage(String message, float time, boolean doNotForce, int align, String style, String name)
    {
        if (doNotForce && BrainOutClient.Actions.getCurrentAction() instanceof OnScreenAction)
        {
            return;
        }

        BrainOutClient.Actions.addAction(new OnScreenAction(time, message, align, style, name));
    }

    private void addOnScreenTimerMessage(String message, float time, boolean doNotForce, int align, String style, String name)
    {
        if (doNotForce && BrainOutClient.Actions.getCurrentAction() instanceof OnScreenAction)
        {
            return;
        }

        BrainOutClient.Actions.addAction(new OnScreenTimerAction(time, message, align, style, name));
    }

    private void addMessage(final Actor data)
    {
        killList.addActor(data);
        killPane.setScrollPercentY(1);

        data.addAction(Actions.sequence(
            Actions.delay(ClientConstants.Menu.KillList.APPEARANCE),
            Actions.alpha(0, ClientConstants.Menu.KillList.ALPHA_TIME),
            Actions.run(() -> killList.removeActor(data))
        ));
    }

    private void addKill(KillEvent event)
    {
        if (event.killer == null || event.victim == null) return;

        Label killer = new Label(event.killer.getName(), BrainOutClient.Skin, "title-small-white");
        killer.setEllipsis(true);

        Label victim = new Label(event.victim.getName(), BrainOutClient.Skin, "title-small-white");
        killer.setEllipsis(true);

        ClientController CC = BrainOutClient.ClientController;

        killer.setColor(CC.getColorOf(event.killer));
        victim.setColor(CC.getColorOf(event.victim));

        String weaponText;

        switch (event.kind)
        {
            case headshot:
            {
                weaponText = L.get("MENU_HEADSHOT");
                break;
            }
            case normal:
            default:
            {
                weaponText = event.instrument.getDefaultSkin().getTitle().get();
                break;
            }
        }

        Label weapon = new Label("[" + weaponText + "]", BrainOutClient.Skin, "title-weapon");

        final Table data = new Table(BrainOutClient.Skin);
        data.align(Align.right);
        data.setBackground(new NinePatchDrawable(BrainOutClient.getNinePatch("form-default")));

        data.add(killer).width(Math.min(killer.getPrefWidth(), 220)).padRight(8).padLeft(4);
        data.add(weapon).padLeft(4).padRight(4);
        data.add(victim).width(Math.min(victim.getPrefWidth(), 220)).padLeft(8).padRight(4);

        addMessage(data);
    }

    public void overrideEscape(Runnable runnable)
    {
        this.escapeOverride = runnable;
    }

    private boolean isAtFocus()
    {
        GameState gs = getGameState();

        return gs != null && gs.hasTopMenu() && getGameState().topMenu() == this;
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        updatePlayerInfoCounter -= delta;
        if (updatePlayerInfoCounter < 0)
        {
            updatePlayerInfo();
            updatePlayerInfoCounter = 2.0f;
        }

        activatorCheck -= delta;

        if (activatorCheck <= 0)
        {
            activatorCheck = 0.25f;

            ClientActiveActivatorComponentData activator = getClosestActivator();

            if (activator == null)
            {
                if (currentActivator != null)
                {
                    activation.clearActions();

                    activation.addAction(
                            Actions.sequence(
                                    Actions.alpha(0, 0.25f),
                                    Actions.visible(false)));

                    currentActivator = null;
                    currentActivatorState = false;
                }
            }
            else
            {
                if (activator.test(myPlayerData))
                {
                    if (!currentActivatorState || activator != currentActivator || activator.dirty())
                    {
                        int code = BrainOutClient.ClientSett.getControls().getKeyCode(
                                KeyProperties.Keys.activate, Input.Keys.E);

                        String key = Input.Keys.toString(code);

                        activation.clearActions();

                        activation.setText(
                            L.get("MENU_ACTIVATION_HINT", key, L.get(activator.getActivateText()))
                        );
                        activation.getColor().set(1, 1, 1, 0);
                        activation.setVisible(true);

                        activation.addAction(Actions.alpha(1.0f, 0.25f));

                        currentActivator = activator;
                        currentActivatorState = true;
                    }
                }
                else
                {
                    if (currentActivatorState || activator != currentActivator)
                    {
                        activation.clearActions();

                        activation.setText(activator.getFailedConditionLocalizedText());
                        activation.getColor().set(1, 0, 0, 0);
                        activation.setVisible(true);

                        activation.addAction(Actions.alpha(1.0f, 0.25f));

                        currentActivator = activator;
                        currentActivatorState = false;
                    }
                }
            }
        }
    }

    @Override
    public void render()
    {
        if (!isRenderBlocked)
            super.render();

        /*
        if (debugRenderer != null && myPlayerData != null)
        {
            Map map = myPlayerData.getMap();

            debugRenderer.render(map.getPhysicWorld(), getCamera().projection);
        }
         */
    }

    @Override
    public void reset()
    {
        disposeMinimap();

        super.reset();

        initWidgets();
        initPlayStateWidgets();
    }
}
