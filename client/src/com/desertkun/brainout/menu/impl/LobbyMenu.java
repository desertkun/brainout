package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.net.HttpParametersUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.client.states.CSQuickPlay;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.content.GlobalConflict;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.shop.ShopCart;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SettingsUpdatedEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.managers.LocalizationManager;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.AlertPopup;
import com.desertkun.brainout.menu.popups.ExitPopup;
import com.desertkun.brainout.menu.tutorial.AnchorTutorialMenu;
import com.desertkun.brainout.menu.tutorial.RichTutorialMenu;
import com.desertkun.brainout.menu.tutorial.Tutorials;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.ui.Tooltip;
import com.desertkun.brainout.menu.widgets.chat.ChatWidget;
import com.desertkun.brainout.menu.widgets.chat.LobbyChatWidget;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.Matchmaking;
import com.desertkun.brainout.online.RoomSettings;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.utils.VersionCompare;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.*;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Objects;
import java.util.TimerTask;

public class LobbyMenu extends PlayerSelectionMenu
{
    private Table controls;
    private RoomSettings roomSettings;
    private float checkTimer;
    private Label playersOnline;
    private TextButton findGame;
    private TextButton chatButton;
    private TextButton quickPlay;
    private Image chatBadge;
    private Image contractsBadge;
    private TextButton blogButton;

    public LobbyMenu(ShopCart shopCart)
    {
        super(shopCart);

        this.roomSettings = new RoomSettings();
        this.roomSettings.init(BrainOutClient.ClientController.getUserProfile(), true);

        Preferences preferences = Gdx.app.getPreferences("region");
        String region = preferences.getString("region", BrainOutClient.ClientController.getMyRegion());
        this.roomSettings.setRegion(region);

        this.checkTimer = 0;
    }

    @Override
    public Table createUI()
    {
        this.controls = new Table();

        return super.createUI();
    }

    @Override
    protected void updateInfo()
    {
        super.updateInfo();

        updateControlsInfo();
        updateRoomSettings();
    }

    public void initWidgets()
    {
        LobbyChatWidget chat = new LobbyChatWidget(
            ClientConstants.Menu.Chat.OFFSET_X,
            ClientConstants.Menu.Chat.OFFSET_Y,
            ClientConstants.Menu.Chat.WIDTH,
            ClientConstants.Menu.Chat.HEIGHT);

        GameState gs = getGameState();

        if (gs == null)
            return;

        gs.getWidgets().addWidget(chat);
    }

    public void checkBanners()
    {
        BrainOutClient.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                Gdx.app.postRunnable(() -> doCheckBanners());
            }
        }, 250);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                SimpleEvent e = ((SimpleEvent) event);

                switch (e.getAction())
                {
                    case socialMessagesReceived:
                    {
                        checkSocialMessagedBadge();

                        return true;
                    }
                    case updateSocialMessages:
                    {
                        checkSocialMessagedBadge();

                        return true;
                    }
                }

                break;
            }
        }

        return super.onEvent(event);
    }

    private void checkSocialMessagedBadge()
    {
        chatBadge.clearActions();

        if (BrainOutClient.SocialController.getMessages().haveUnreadMessages())
        {
            chatBadge.setVisible(true);

            chatBadge.addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
                Actions.delay(0.5f),
                Actions.run(() -> chatBadge.setVisible(!chatBadge.isVisible()))
            )));
        }
        else
        {
            chatBadge.setVisible(false);
        }

    }

    private void doCheckBanners()
    {
        Tooltip.Hide();

        getRootActor().validate();

        int childOffset = 0;

        if (!Tutorials.IsFinished("primary"))
        {
            Table node = (Table) slotsContent.getChildren().get(childOffset);

            pushMenu(new AnchorTutorialMenu(
                    node,
                    "tutorial-hint-primary", -28, -16,
                    L.get("MENU_TUTORIAL_HINT_PRIMARY"),
                    28, 190, 200, 48,
                    () -> {
                        Tutorials.Done("primary");
                        checkBanners();
                    }
            ));

            return;
        }

        if (!Tutorials.IsFinished("secondary"))
        {
            Table node = (Table) slotsContent.getChildren().get(childOffset + 1);

            pushMenu(new AnchorTutorialMenu(
                    node,
                    "tutorial-hint-secondary", -78, -16,
                    L.get("MENU_TUTORIAL_HINT_SECONDARY"),
                    28, 190, 200, 48,
                    () -> {
                        Tutorials.Done("secondary");
                        checkBanners();
                    }
            ));

            return;
        }

        if (!Tutorials.IsFinished("special-1"))
        {
            Table node = (Table) slotsContent.getChildren().get(childOffset + 2);

            pushMenu(new AnchorTutorialMenu(
                    node,
                    "tutorial-hint-special-1", -78, -16,
                    L.get("MENU_TUTORIAL_HINT_SPECIAL_1"),
                    28, 190, 200, 48,
                    () -> {
                        Tutorials.Done("special-1");
                        checkBanners();
                    }
            ));

            return;
        }

        if (!Tutorials.IsFinished("special-2"))
        {
            Table node = (Table) slotsContent.getChildren().get(childOffset + 2);

            pushMenu(new AnchorTutorialMenu(
                node,
                "tutorial-hint-special-2", -78, 48,
                L.get("MENU_TUTORIAL_HINT_SPECIAL_2"),
                28, 190, 200, 48,
                () -> {
                    Tutorials.Done("special-2");
                    checkBanners();
                }
            ));

            return;
        }

        if (!Tutorials.IsFinished("fight"))
        {
            pushMenu(new AnchorTutorialMenu(
                quickPlay,
                "tutorial-hint-fight", -34, -48,
                L.get("MENU_TUTORIAL_HINT_FIGHT_TYPE"),
                28, 190, 200, 48,
                () -> {
                    Tutorials.Done("fight");
                    checkBanners();
                }
            ));

            return;
        }

        boolean containers = false;

        if (BrainOutClient.ClientController.getUserProfile() != null)
        {
            float score = BrainOutClient.ClientController.getUserProfile().getStats().get(
                    Constants.User.SCORE, 0f);

            containers = score > 50;
        }

        if (containers && !Tutorials.IsFinished("container"))
        {
            pushMenu(new AnchorTutorialMenu(
                    userPanel.getCaseButton(),
                    "tutorial-hint-container", -232, -232,
                    L.get("MENU_TUTORIAL_HINT_CONTAINERS"),
                    28, 96, 200, 48,
                    () -> {
                        Tutorials.Done("container");
                        checkBanners();
                    }
            ));

            return;
        }

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        if (userProfile != null && userProfile.getStats().get(Constants.Stats.TIME_SPENT, 0.0f) > 5)
        {
            if (!Tutorials.IsFinished("visibility-1"))
            {
                pushMenu(new RichTutorialMenu(L.get("MENU_TUTORIAL_VIS_1"), () -> {
                    Tutorials.Done("visibility-1");
                    checkBanners();
                }));

                return;
            }

            if (!Tutorials.IsFinished("visibility-2"))
            {
                pushMenu(new RichTutorialMenu(L.get("MENU_TUTORIAL_VIS_2"), () -> {
                    Tutorials.Done("visibility-2");
                    checkBanners();
                }));

                return;
            }

            if (!Tutorials.IsFinished("visibility-3"))
            {
                pushMenu(new RichTutorialMenu(L.get("MENU_TUTORIAL_VIS_3"), () -> {
                    Tutorials.Done("visibility-3");
                    checkBanners();
                }));

                return;
            }
        }

        if (Greenlight.show())
        {
            return;
        }

        if (StoreBanner.show())
        {
            return;
        }
    }

    @Override
    public void onInit()
    {
        super.onInit();

        getRootActor().layout();

        checkBanners();
        initWidgets();
        checkFreePlay();
        setupWatcher();

        if (checkBlogs())
        {
            openBlogs();
        }
    }

    private boolean checkBlogs()
    {
        BlogService.BlogEntriesList blogEntries = BrainOutClient.ClientController.getBlogEntries();
        if (blogEntries == null || blogEntries.isEmpty())
            return false;

        BlogService.BlogEntry fistEntry = blogEntries.getFirst();
        int id = fistEntry.id;

        Preferences pref = Gdx.app.getPreferences("blog");

        if (!pref.contains("id") || pref.getInteger("id", 0) != id)
        {
            pref.putInteger("id", id);
            pref.flush();

            return true;
        }

        return false;
    }

    private void setupWatcher()
    {
        Map.SetWatcher(new Watcher()
        {
            @Override
            public float getWatchX()
            {
                CSGame state = BrainOutClient.ClientController.getState(CSGame.class);

                if (state == null)
                    return 0;

                PlayerData playerData = state.getPlayerData();
                return playerData != null ? playerData.getX() : 0;
            }

            @Override
            public float getWatchY()
            {
                CSGame state = BrainOutClient.ClientController.getState(CSGame.class);

                if (state == null)
                    return 0;

                PlayerData playerData = state.getPlayerData();
                return playerData != null ? playerData.getY() : 0;
            }

            @Override
            public boolean allowZoom()
            {
                return false;
            }

            @Override
            public float getScale()
            {
                return 1.0f;
            }

            @Override
            public String getDimension()
            {
                CSGame state = BrainOutClient.ClientController.getState(CSGame.class);

                if (state == null)
                    return "default";

                PlayerData playerData = state.getPlayerData();

                if (playerData == null)
                    return "default";

                return playerData.getDimension();
            }
        });
    }

    private void checkFreePlay()
    {
        if (BrainOutClient.ConnectFreePlayPartyId != null)
        {
            Matchmaking.JoinFreePlay(BrainOutClient.ConnectFreePlayPartyId);
            BrainOutClient.ConnectFreePlayPartyId = null;
        }
    }

    @Override
    protected boolean shouldOpenSlotAtStart()
    {
        return false;
    }

    private void updateControlsInfo()
    {
        playersOnline = new Label("", BrainOutClient.Skin, "title-green");
        playersOnline.setAlignment(Align.center);

        TextButton base = new TextButton(
            L.get("MENU_HOME_BASE"),
            BrainOutClient.Skin, "button-default");

        quickPlay = new TextButton(
            L.get("MENU_QUICK_PLAY"),
            BrainOutClient.Skin, "button-activated");

        TextButton freePlay = new TextButton(
            L.get("MENU_FREE_PLAY"),
            BrainOutClient.Skin, "button-default");

        findGame = new TextButton(
            L.get("MENU_FIND_GAME"),
            BrainOutClient.Skin, "button-default");
        TextButton profile = new TextButton(
            L.get("MENU_CONNECT_ACCOUNT"),
            BrainOutClient.Skin, "button-default");

        base.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                goToBase();
            }
        });

        quickPlay.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                quickPlay();
            }
        });

        freePlay.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                freePlay();
            }
        });

        findGame.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                findGame();
            }
        });

        profile.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                openUserProfile();
            }
        });

        controls.add(playersOnline).colspan(3).expandX().pad(8).padTop(0).fillX().row();

        controls.add(base).colspan(3).expandX().height(32).fillX().row();
        controls.add(quickPlay).colspan(3).expandX().height(32).fillX().row();
        controls.add(freePlay).colspan(3).expandX().height(32).fillX().row();
        controls.add(findGame).colspan(3).expandX().height(32).fillX().row();
        controls.add(profile).colspan(3).expandX().height(32).fillX().row();

        {
            {
                Button authors = new Button(BrainOutClient.Skin, "button-notext");

                {
                    Image img = new Image(BrainOutClient.Skin, "icon-authors");
                    img.setScaling(Scaling.none);
                    img.setTouchable(Touchable.disabled);
                    authors.add(img);
                }

                Tooltip.RegisterToolTip(authors, L.get("MENU_AUTHORS"), this);

                authors.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pushMenu(new AuthorsMenu());
                    }
                });

                controls.add(authors).expandX().height(32).fillX();
            }

            {
                Button settings = new Button(BrainOutClient.Skin, "button-notext");

                {
                    Image img = new Image(BrainOutClient.Skin, "icon-settings");
                    img.setScaling(Scaling.none);
                    img.setTouchable(Touchable.disabled);
                    settings.add(img);
                }

                Tooltip.RegisterToolTip(settings, L.get("MENU_SETTINGS"), this);

                settings.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pushMenu(new SettingsMenu());
                    }
                });

                controls.add(settings).expandX().height(32).fillX();
            }

            {
                Button exit = new Button(BrainOutClient.Skin, "button-red-border");

                {
                    Image img = new Image(BrainOutClient.Skin, "icon-exit");
                    img.setScaling(Scaling.none);
                    img.setTouchable(Touchable.disabled);
                    exit.add(img);
                }

                Tooltip.RegisterToolTip(exit, L.get("MENU_EXIT"), this);

                exit.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        showExitPrompt();
                    }
                });

                controls.add(exit).expandX().height(32).fillX().row();
            }
        }
    }

    private void showExitPrompt()
    {
        pushMenu(new ExitPopup());
    }

    private void setPlayerScale(float scale)
    {
        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);

        if (csGame == null)
            return;

        PlayerData playerData = csGame.getPlayerData();

        if (playerData == null)
            return;

        ClientPlayerComponent cpc = playerData.getComponent(ClientPlayerComponent.class);

        if (cpc == null)
            return;

        cpc.setScale(scale);
    }

    public void goToBase()
    {
        GameState gs = getGameState();

        if (gs == null)
            return;

        pop();

        setPlayerScale(1.0f);

        Menu topMenu = gs.topMenu();

        if (topMenu instanceof ActionPhaseMenu)
        {
            ActionPhaseMenu aps = ((ActionPhaseMenu) topMenu);

            aps.overrideEscape(() -> gs.pushMenu(new LobbyMenu(getShopCart())));
        }

        CSGame csGame = BrainOutClient.ClientController.getState(CSGame.class);

        if (csGame != null)
        {
            PlayerData playerData = BrainOutClient.ClientController.getState(CSGame.class).getPlayerData();

            if (playerData != null)
            {
                ClientPlayerComponent cpc = playerData.getComponent(ClientPlayerComponent.class);

                if (cpc != null)
                {
                    Map.SetWatcher(cpc);
                }
            }
        }
    }

    private void freePlay()
    {
        pushMenu(new FreePlayQuestsMenu());
    }

    private void openUserProfile()
    {
        pushMenu(new PlayerProfileMenu()
        {
            @Override
            protected void receive(ProfileCallback callback)
            {
                Gdx.app.postRunnable(() ->
                    callback.received(true, BrainOutClient.ClientController.getUserProfile()));
            }

            @Override
            protected String getSteamID()
            {
                return null;
            }

            @Override
            protected String getAccountID()
            {
                return BrainOutClient.ClientController.getMyAccount();
            }

            @Override
            protected boolean hasComplaintsButton()
            {
                return false;
            }
        });
    }

    private void openUserProfileInBrowser()
    {
        LoginService loginService = LoginService.Get();

        LoginService.AccessToken token = loginService.getCurrentAccessToken();

        HashMap<String, String> args = new HashMap<>();
        args.put("access_token", token.get());

        Object www = EnvironmentService.Get().getEnvironmentVariables().get("www");

        if (www == null)
            return;

        BrainOutClient.Env.openURI(www.toString() +
            "/import?" + HttpParametersUtils.convertHttpParameters(args));
    }

    @Override
    public boolean escape()
    {
        goToBase();

        return true;
    }

    private void findGame()
    {
        if (!BrainOut.OnlineEnabled())
        {
            BrainOutClient.getInstance().topState().pushMenu(new AlertPopup(
                    BrainOutClient.Env.getOfflineBuildError("Run Server Game Phase.bat")
            ));
            return;
        }

        RoomSettings roomSettings1 = new RoomSettings();
        roomSettings1.setRegion(BrainOutClient.ClientController.getMyRegion());
        roomSettings1.init(BrainOutClient.ClientController.getUserProfile(), false);

        long conflictStart = 0;
        String myClanId;

        if (BrainOutClient.SocialController.getMyClan() != null)
        {
            myClanId = BrainOutClient.SocialController.getMyClan().getId();
        }
        else
        {
            myClanId = null;
        }

        pushMenu(new GlobalConflictMenu(new GlobalConflictMenu.Callback()
        {
            @Override
            public void selected(GameService.Room room)
            {
                Matchmaking.JoinRoom(room);
            }

            @Override
            public void cancelled()
            {
                //
            }

            @Override
            public void newOne(String zoneKey)
            {
                roomSettings1.setZone(zoneKey);

                find("main", roomSettings1, new Matchmaking.FindGameResult()
                {
                    @Override
                    public void success(String roomId)
                    {
                        //
                    }

                    @Override
                    public void failed(Request.Result status, Request request)
                    {
                        //
                    }

                    @Override
                    public void connectionFailed()
                    {

                    }
                }, true);
            }
        }, roomSettings1, conflictStart,
            GlobalConflict.GetAccountOwner(BrainOutClient.ClientController.getMyAccount(), myClanId, conflictStart)));
    }

    @Override
    protected void userProfileUpdated()
    {
        super.userProfileUpdated();

        updateRoomSettings();
    }

    @Override
    public void act(float delta)
    {
        super.act(delta);

        checkTimer -= delta;

        if (checkTimer < 0)
        {
            checkTimer = 20;

            updateGamesStatus();
        }
    }

    private void updateGamesStatus()
    {
        if (!BrainOut.OnlineEnabled())
            return;

        GameService gameService = GameService.Get();

        if (gameService != null)
        {
            gameService.getStatus(
                (service, request, result, status) ->
            {
                if (result == Request.Result.success)
                {
                    Gdx.app.postRunnable(() -> updatePlayersOnline(status.players));
                }
            });
        }
    }

    private void updatePlayersOnline(int amount)
    {
        roomSettings.setLevelGap(calculateLevelGap(amount));

        playersOnline.setText(
            L.get("MENU_PLAYERS_ONLINE", String.valueOf(amount))
        );
    }

    private int calculateLevelGap(int amount)
    {
        if (amount <= 80)
            return 100;

        if (amount <= 160)
            return 32;

        if (amount <= 320)
            return 16;

        return 8;
    }

    public void updateRoomSettings()
    {
        roomSettings.setLevel(BrainOutClient.ClientController.getUserProfile().getLevel(
            Constants.User.LEVEL
        ));
    }

    private void quickPlay()
    {
        if (!BrainOut.OnlineEnabled())
        {
            BrainOutClient.getInstance().topState().pushMenu(new AlertPopup(
                BrainOutClient.Env.getOfflineBuildError("Run Server Game Phase.bat")
            ));
            return;
        }

        pushMenu(new QuickPlayOptionsMenu(new QuickPlayOptionsMenu.Callback()
        {
            @Override
            public void selected(String name, RoomSettings settings, QuickPlayOptionsMenu menu)
            {
                find(name, settings, new Matchmaking.FindGameResult()
                {
                    @Override
                    public void success(String roomId)
                    {
                        //
                    }

                    @Override
                    public void failed(Request.Result status, Request request)
                    {
                        //
                    }

                    @Override
                    public void connectionFailed()
                    {

                    }
                }, name.equals("main") || name.equals("custom"));
            }

            @Override
            public void cancelled()
            {
                //
            }
        }, roomSettings));
    }

    private void find(String name, RoomSettings settings, Matchmaking.FindGameResult result,
                      boolean trackStarted)
    {
        BrainOutClient.ClientController.setState(new CSQuickPlay(name, settings, result, trackStarted));
    }

    @Override
    protected void updateSlotContents(Table custom, Table slotsContent, ButtonGroup<TextButton> buttonGroup)
    {
        super.updateSlotContents(custom, slotsContent, buttonGroup);

        Table buttons = new Table();
        buttons.align(Align.left);

        custom.add(buttons).padBottom(8).bottom().height(138).expandX().left();

        {
            Table buttonsGroup = new Table();
            buttons.add(buttonsGroup).size(64, 138).padLeft(12).left();

            {
                TextureRegion icon = BrainOutClient.getRegion("icon-wiki");
                if (icon != null)
                {
                    TextButton chatButton = new TextButton("", BrainOutClient.Skin, "button-default");

                    Image image = new Image(icon);
                    image.setScaling(Scaling.none);
                    image.setFillParent(true);
                    image.setTouchable(Touchable.disabled);
                    chatButton.addActor(image);

                    chatButton.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            openWiki();
                        }
                    });

                    buttonsGroup.add(chatButton).size(64, 64).padBottom(10).row();
                }
            }
            {
                TextureRegion icon = BrainOutClient.getRegion("icon-chat");
                if (icon != null)
                {
                    chatButton = new TextButton("", BrainOutClient.Skin, "button-default");

                    Image image = new Image(icon);
                    image.setScaling(Scaling.none);
                    image.setFillParent(true);
                    image.setTouchable(Touchable.disabled);
                    chatButton.addActor(image);

                    chatButton.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            openLobbyChat();
                        }
                    });

                    buttonsGroup.add(chatButton).size(64, 64).row();

                    chatBadge = new Image(BrainOutClient.Skin, "badge");
                    chatBadge.setScaling(Scaling.none);
                    chatBadge.setAlign(Align.left | Align.top);
                    chatBadge.setFillParent(true);
                    chatBadge.setTouchable(Touchable.disabled);
                    chatBadge.setVisible(false);

                    chatButton.addActor(chatBadge);
                }
            }
        }

        {
            Table buttonsGroup = new Table();
            buttonsGroup.align(Align.bottom);
            buttons.add(buttonsGroup).size(64, 138).padLeft(12).left();

            /*
            {
                TextButton btn = new TextButton("", BrainOutClient.Skin, "button-default");

                Image image = new Image(BrainOutClient.Skin, "icon-contracts");
                image.setScaling(Scaling.none);
                image.setFillParent(true);
                image.setTouchable(Touchable.disabled);
                btn.addActor(image);

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pushMenu(new ContractsMenu());
                    }
                });

                contractsBadge = new Image(BrainOutClient.Skin, "badge");
                contractsBadge.setScaling(Scaling.none);
                contractsBadge.setAlign(Align.left | Align.top);
                contractsBadge.setFillParent(true);
                contractsBadge.setTouchable(Touchable.disabled);
                contractsBadge.setVisible(false);

                btn.addActor(contractsBadge);

                buttonsGroup.add(btn).size(64, 64).padBottom(10).row();
            }
             */

            TextureRegion icon = BrainOutClient.getRegion("icon-discord");
            if (icon != null)
            {
                TextButton chatButton = new TextButton("", BrainOutClient.Skin, "button-default");

                Image image = new Image(icon);
                image.setScaling(Scaling.none);
                image.setFillParent(true);
                image.setTouchable(Touchable.disabled);
                chatButton.addActor(image);

                chatButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        boolean success = Gdx.net.openURI("https://discord.gg/brainout");

                        if (success)
                        {
                            Gdx.app.postRunnable(() ->
                                    BrainOutClient.getInstance().topState().pushMenu(new AlertPopup(L.get("MENU_BROWSER_TAB"))));
                        }
                    }
                });

                buttonsGroup.add(chatButton).size(64, 64).row();
            }
        }

        if (!BrainOut.OnlineEnabled())
            return;

        {

            TextureRegion icon = BrainOutClient.getRegion("icon-blog");
            if (icon != null)
            {
                blogButton = new TextButton("", BrainOutClient.Skin, "button-checkable");

                if (getOpenWindowMode() == OpenWindowMode.blog)
                {
                    blogButton.setChecked(true);
                }

                Image image = new Image(icon);
                image.setScaling(Scaling.none);
                image.setFillParent(true);
                image.setTouchable(Touchable.disabled);
                blogButton.addActor(image);

                blogButton.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        openBlogs();
                    }
                });

                buttons.add(blogButton).expandY().bottom().size(64, 64).padLeft(12).left();
            }

        }
    }

    @Override
    protected void cleanUpWindowMode()
    {
        super.cleanUpWindowMode();

        if (getOpenWindowMode() == OpenWindowMode.blog)
        {
            blogButton.setChecked(false);
        }
    }

    private void openBlogs()
    {
        getMainContent().clear();

        if (getOpenWindowMode() == OpenWindowMode.blog)
        {
            setOpenWindowMode(OpenWindowMode.none);
            return;
        }

        BlogService.BlogEntriesList blogEntries = BrainOutClient.ClientController.getBlogEntries();
        if (blogEntries == null || blogEntries.isEmpty())
            return;

        setOpenWindowMode(OpenWindowMode.blog);

        if (blogButton != null)
        {
            blogButton.setChecked(true);
        }

        {
            Table header = new Table(BrainOutClient.Skin);
            header.setBackground("form-gray");

            Label title = new Label(L.get("MENU_NEWS"), BrainOutClient.Skin, "title-small");
            title.setAlignment(Align.center);
            header.add(title).expand().center();

            TextButton close = new TextButton("x", BrainOutClient.Skin, "button-default");


            close.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    if (blogButton != null)
                    {
                        blogButton.setChecked(false);
                    }

                    getMainContent().clear();

                    if (getOpenWindowMode() == OpenWindowMode.blog)
                    {
                        setOpenWindowMode(OpenWindowMode.none);
                    }
                }
            });

            header.add(close).size(40, 30).right().row();

            header.pad(0).padTop(2).padRight(1);

            getMainContent().add(header).expandX().fillX().padTop(16).row();
        }

        {
            Table payload = new Table(BrainOutClient.Skin);
            payload.setBackground("form-default");

            Table contents = new Table();
            contents.align(Align.top);

            renderBlogItems(contents, blogEntries);

            ScrollPane news = new ScrollPane(contents, BrainOutClient.Skin, "scroll-default");
            payload.add(news).expand().fill().row();
            setScrollFocus(news);

            getMainContent().add(payload).expand().fill().padBottom(16).row();
        }

    }

    private void renderBlogItems(Table contents, BlogService.BlogEntriesList blogEntries)
    {
        for (BlogService.BlogEntry entry : blogEntries)
        {
            JSONObject image = entry.data.optJSONObject("image");
            JSONObject titleValue = entry.data.optJSONObject("title");
            JSONObject descriptionValue = entry.data.optJSONObject("description");

            if (image == null || titleValue == null || descriptionValue == null)
                return;

            String version = titleValue.optString("version", null);
            if (version != null && !version.isEmpty())
            {
                VersionCompare v = new VersionCompare(version);
                VersionCompare current = new VersionCompare(Version.VERSION);

                if (current.compareTo(v) < 0)
                {
                    continue;
                }
            }

            String titleText = titleValue.optString(BrainOutClient.LocalizationMgr.getCurrentLanguage().toLowerCase(),
                titleValue.optString(LocalizationManager.GetDefaultLanguage().toLowerCase(), null));

            String descriptionText = descriptionValue.optString(BrainOutClient.LocalizationMgr.getCurrentLanguage().toLowerCase(),
                descriptionValue.optString(LocalizationManager.GetDefaultLanguage().toLowerCase(), null));

            if (titleText == null || descriptionText == null)
                continue;

            String imageUrl = image.optString(BrainOutClient.LocalizationMgr.getCurrentLanguage().toLowerCase(),
                image.optString(LocalizationManager.GetDefaultLanguage().toLowerCase(), null));

            if (imageUrl == null)
                continue;

            Button btn = new Button(BrainOutClient.Skin, "button-row-default");

            Image background = new Image();
            background.setScaling(Scaling.fit);

            Avatars.GetAndCache(imageUrl, (has, avatar) ->
            {
                if (has)
                {
                    background.setDrawable(new TextureRegionDrawable(avatar));
                }
            });

            btn.add(background).expandX().fillX().minHeight(300).row();

            Table splash = new Table(BrainOutClient.Skin);
            splash.align(Align.top);
            splash.setBackground("form-transparent-top");

            {
                Label title = new Label(DateFormat.getDateTimeInstance().format(entry.dateCreate),
                    BrainOutClient.Skin, "title-small");
                splash.add(title).expandX().fillX().padBottom(16).row();
            }

            {
                Label title = new Label(titleText, BrainOutClient.Skin, "title-yellow");
                splash.add(title).expandX().fillX().padBottom(16).row();
            }

            {
                Label description = new Label(descriptionText.replace("\n", " ").substring(0, 96) + "...", BrainOutClient.Skin, "title-small");
                description.setWrap(true);
                splash.add(description).expandX().fillX().row();
            }

            btn.add(splash).pad(0, -4, -6, -4).expandX().fillX().height(120).padTop(-120).row();

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    pushMenu(new BlogEntryMenu(entry));
                }
            });

            contents.add(btn).pad(8).expandX().fillX().row();
        }
    }

    private void openLobbyChat()
    {
        if (BrainOutClient.SocialController.getMessages().haveUnreadMessages())
        {
            ChatWidget.Open("clan");
        }
        else
        {
            ChatWidget.Open();
        }
    }

    private void openWiki()
    {
        if (BrainOutClient.LocalizationMgr.getCurrentLanguage().equals("RU"))
        {
            BrainOutClient.Env.openURI("https://brainout.fandom.com/ru");
        }
        else
        {
            BrainOutClient.Env.openURI("https://brainout.fandom.com");
        }
    }

    @Override
    protected void setUpRightPanel(Table rightContent)
    {
        super.setUpRightPanel(rightContent);

        rightContent.add(controls).pad(8).padTop(4).width(192).expandX().fillX().right().top().row();
    }
}
