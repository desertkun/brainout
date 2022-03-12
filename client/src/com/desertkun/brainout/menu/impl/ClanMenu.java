package com.desertkun.brainout.menu.impl;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.menu.Menu;
import com.desertkun.brainout.menu.popups.*;
import com.desertkun.brainout.menu.ui.*;
import com.desertkun.brainout.menu.widgets.chat.LobbyChatWidget;
import com.desertkun.brainout.online.*;
import com.desertkun.brainout.utils.StringFunctions;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.EventService;
import org.anthillplatform.runtime.services.GameService;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.SocialService;
import org.anthillplatform.runtime.util.JsonRPC;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.*;
import java.util.List;

public class ClanMenu extends Menu implements EventReceiver
{
    private final String clanId;
    private Table data;
    private Clan clan;
    private Table leftButtons, rightButtons;

    private Label sortingTitle;
    private Table membersList;
    private int columnWidth;
    private SortingFunction sortingFunction;

    private ObjectMap<String, Float> currentTournament;
    private OrderedMap<String, GameService.PartyMember> conflictPartyMembers;
    private Map<String, List<GameService.PlayerRecord>> playerRecords;
    private boolean inTournament;

    // conflict
    private Clan conflict;
    private GameService.PartySession conflictSession;
    private Table conflictTable;
    private GameService.Party conflictPartyInfo;
    private String conflictPartyId;
    private Table conflictChat;
    private Table conflictChatMessages;
    private ScrollPane messagesPane;

    private class ClanMenuConflictSessionListener implements GameService.PartySession.Listener
    {
        @Override
        public void onError(int code, String message, String data)
        {
            if (Log.ERROR) Log.error("Party session error: " + code + " " + message + " " + data);

            Gdx.app.postRunnable(() -> pushMenu(new RichAlertPopup(L.get("MENU_ATTENTION"), message)));
        }

        @Override
        public void onError(Exception e)
        {
            if (Log.ERROR) Log.error("Party session error: " + e.getLocalizedMessage());

            Gdx.app.postRunnable(() -> pushMenu(new RichAlertPopup(L.get("MENU_ATTENTION"), e.getLocalizedMessage())));
        }

        @Override
        public void onOpen()
        {
            if (Log.INFO) Log.info("Party session opened!");
        }

        @Override
        public void onClose(int code, String message, boolean remote)
        {
            if (Log.INFO) Log.info("Party session closed: " + code + " " + message);

            Gdx.app.postRunnable(() ->
            {
                conflictPartyMembers.clear();

                switch (code)
                {
                    case -1:
                    case 1006:
                    {
                        BrainOutClient.Timer.schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                Gdx.app.postRunnable(ClanMenu.this::reset);
                            }
                        }, 5000);

                        break;
                    }
                    case 3410:
                    {
                        BrainOutClient.Timer.schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                Gdx.app.postRunnable(ClanMenu.this::reset);
                            }
                        }, 1000);

                        break;
                    }
                    case 3404:
                    {
                        checkConflict();
                        break;
                    }
                    case 3411:
                    {
                        break;
                    }
                    default:
                    {
                        renderConflictLoading();

                        BrainOutClient.Timer.schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                Gdx.app.postRunnable(ClanMenu.this::openConflictSession);
                            }
                        }, 1);

                        break;
                    }
                }
            });
        }

        @Override
        public void onPartyInfoReceived(GameService.Party party, java.util.List<GameService.PartyMember> members)
        {
            if (Log.INFO) Log.info("Party info received: " + party.getId() + " with " + members.size() + " members");

            Gdx.app.postRunnable(() ->
            {
                for (GameService.PartyMember member : members)
                {
                    conflictPartyMembers.put(member.getAccount(), member);
                }

                conflictPartyInfo = party;
                conflictPartyId = party.getId();

                renderConflict();

                Cell cell = onNewConflictSimpleMessage(L.get("MENU_CLAN_CHALLENGE_WELCOME"));

                if (cell != null)
                {
                    cell.padTop(32).padBottom(32);
                }
            });
        }

        @Override
        public void onPlayerJoined(GameService.PartyMember member)
        {
            if (Log.INFO) Log.info("Party session player joined!");

            Gdx.app.postRunnable(() ->
            {
                conflictPartyMembers.put(member.getAccount(), member);

                onNewConflictSimpleMessage(L.get("MP_PLAYER_CONNECTED",
                        member.getProfile().optString("name", "???")));

                renderConflict();
            });
        }

        @Override
        public void onPlayerLeft(GameService.PartyMember member)
        {
            if (Log.INFO) Log.info("Party session player left!");

            Gdx.app.postRunnable(() ->
            {
                conflictPartyMembers.remove(member.getAccount());

                onNewConflictSimpleMessage(L.get("MP_PLAYER_DISCONNECTED",
                    member.getProfile().optString("name", "???")));

                renderConflict();
            });
        }

        @Override
        public void onGameStarting(JSONObject payload)
        {
            if (Log.INFO) Log.info("Game is being started!");

            Gdx.app.postRunnable(ClanMenu.this::renderConflictStarting);
        }

        @Override
        public void onGameStartFailed(int code, String message)
        {
            Gdx.app.postRunnable(() ->
            {
                renderConflict();
                pushMenu(new RichAlertPopup(L.get("MENU_ATTENTION"), message));
            });
        }

        @Override
        public void onGameStarted(String roomId, String slot, String key, String host, ArrayList<Integer> ports, JSONObject roomSettings)
        {
            if (Log.INFO) Log.info("Game started!");

            Gdx.app.postRunnable(() ->
            {
                int[] ports_ = new int[ports.size()];

                for (int i = 0, t = ports.size(); i < t; i++)
                {
                    ports_[i] = ports.get(i);
                }

                Matchmaking.Connect(key, host, ports_, roomSettings, conflictPartyId, () ->
                    pushMenu(new AlertPopup("MENU_CONNECTION_ERROR")));
            });
        }

        @Override
        public void onPartyClosed(JSONObject payload)
        {
            if (Log.INFO) Log.info("Party closed!");

            Gdx.app.postRunnable(() -> onNewConflictSimpleMessage("Conflict is closed."));
        }

        @Override
        public void onCustomMessage(String messageType, JSONObject payload)
        {
            if (Log.INFO) Log.info("Custom message: " + messageType);

            Gdx.app.postRunnable(() -> ClanMenu.this.onCustomConflictMessage(payload));
        }
    }

    private void onCustomConflictMessage(JSONObject payload)
    {
        String type = payload.optString("type");

        if (type == null)
            return;

        switch (type)
        {
            case "chat":
            {
                String sender = payload.optString("sender");
                String clan = payload.optString("clan");
                String text = payload.optString("text");

                if (sender != null && clan != null)
                {
                    onNewConflictChatMessage(sender, clan, text);
                }
            }
        }
    }

    private Cell onNewConflictSimpleMessage(String text)
    {
        if (conflictChatMessages == null)
            return null;

        Table row = new Table();

        {
            Label content = new Label(text, BrainOutClient.Skin, "title-gray");
            content.setAlignment(Align.center);
            content.setWrap(true);

            row.add(content).top().expandX().fillX().row();
        }

        Cell cell = conflictChatMessages.add(row).expandX().fillX().padTop(4);
        cell.row();
        return cell;
    }

    private void onNewConflictChatMessage(String sender, String clan, String text)
    {
        if (conflictChatMessages == null)
            return;

        Table row = new Table();

        {
            Label ttl = new Label(sender + ":", BrainOutClient.Skin, "title-small");
            ttl.setColor(clan.equals(clanId) ? ClientConstants.Menu.KillList.CLAN_COLOR :
                ClientConstants.Menu.KillList.ENEMY_COLOR);

            row.add(ttl).padRight(8).top();
        }

        {
            Label content = new Label(text, BrainOutClient.Skin, "title-small");
            content.setWrap(true);

            row.add(content).top().expandX().fillX().row();
        }

        conflictChatMessages.add(row).expandX().fillX().padTop(4).row();
    }

    private void checkConflict()
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();

        BrainOutClient.SocialController.sendRequest("check_conflict", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private void updateClanMenuControls()
    {
        boolean participating = isParticipatingInConflict();

        rightButtons.clear();

        if (!participating)
        {
            {
                TextButton btn = new TextButton(L.get("MENU_CLOSE"), BrainOutClient.Skin, "button-default");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        close();
                    }
                });

                rightButtons.add(btn).expandX().fillX().height(64).row();
            }

            {
                TextButton btn = new TextButton(L.get("MENU_TOP_100"), BrainOutClient.Skin, "button-default");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        GameState gs = getGameState();

                        if (gs == null)
                            return;

                        close();

                        gs.pushMenu(new BrowseClansMenu());
                    }
                });

                rightButtons.add(btn).expandX().fillX().height(64).row();
            }
        }
    }

    @Override
    public void initTable()
    {
        super.initTable();

        leftButtons = MenuHelper.AddLeftButtonsContainers(this);
        rightButtons = MenuHelper.AddButtonsContainers(this);

        updateClanMenuControls();
    }

    private void close()
    {
        pop();
    }

    @Override
    protected TextureRegion getBackground()
    {
        return BrainOutClient.getRegion("bg-clan");
    }

    @Override
    protected MenuAlign getMenuAlign()
    {
        return MenuAlign.fill;
    }

    @Override
    public Table createUI()
    {
        data = new Table();

        data.add(new LoadingBlock()).pad(32);

        return data;
    }

    private void processClan(SocialService.Group group, boolean inTournament)
    {
        this.clan = new Clan(group);
        this.inTournament = inTournament;

        Clan myClan = BrainOutClient.SocialController.getMyClan();

        if (BrainOutClient.ClientController.isLobby() &&
            this.clan.isInConflict() && myClan != null && myClan.getId().equals(clanId))
        {
            SocialService socialService = SocialService.Get();
            LoginService loginService = LoginService.Get();
            GameService gameService = GameService.Get();

            if (socialService != null && loginService != null && gameService != null)
            {
                socialService.getGroup(
                    loginService.getCurrentAccessToken(),
                    this.clan.getConflictWith(),
                    (service, request, result, conflictGroup) -> Gdx.app.postRunnable(() ->
                {
                    Gdx.app.postRunnable(() ->
                    {
                        if (result == Request.Result.success)
                        {
                            conflict = new Clan(conflictGroup);
                            conflictPartyMembers = new OrderedMap<>();

                            openConflictSession();
                        }

                        renderClan();
                    });
                }));
            }
            else
            {
                renderClan();
            }
        }
        else
        {
            renderClan();
        }
    }

    private void openConflictSession()
    {
        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        conflictSession = gameService.openExistingPartySession(
            this.clan.getConflictPartyId(),
            null, null, false,
            loginService.getCurrentAccessToken(),
            new ClanMenuConflictSessionListener());
    }

    private void renderClan()
    {
        if (BrainOutClient.SocialController.getMyClan() != null &&
                BrainOutClient.SocialController.getMyClan().getId().equals(clan.getId()))
        {
            BrainOutClient.SocialController.updateMyClan(this.clan);
        }

        data.clear();

        Table contents = new Table();
        contents.align(Align.top);

        renderLeftButtons();
        renderContents(contents);

        ScrollPane scrollPane = new ScrollPane(contents, BrainOutClient.Skin, "scroll-default");
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        data.add(scrollPane).expand().fill().pad(20).row();

        setScrollFocus(scrollPane);

        checkUserAvatar();

        checkMemberStatus();
    }

    private void checkMemberStatus()
    {
        updateMembersStatus();

        addAction(Actions.repeat(RepeatAction.FOREVER, Actions.sequence(
            Actions.delay(30),
            Actions.run(this::updateMembersStatus)
        )));
    }

    private void updateMembersStatus()
    {
        if (!BrainOut.OnlineEnabled())
            return;

        LoginService loginService = LoginService.Get();
        GameService gameService = GameService.Get();

        if (loginService == null || gameService == null)
            return;

        ArrayList<String> accounts = new ArrayList<>();

        for (ObjectMap.Entry<String, Clan.ClanMember> entry : clan.getMembers())
        {
            accounts.add(entry.key);
        }

        gameService.listMultipleAccountsRecords(loginService.getCurrentAccessToken(), accounts,
            (service, request, result, records) -> Gdx.app.postRunnable(() ->
        {
            if (result == Request.Result.success)
            {
                this.playerRecords = records;

                if (sortingFunction != null)
                {
                    renderSorting();
                }
            }
        }));
    }

    private void checkUserAvatar()
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        if (!userProfile.isParticipatingClan())
            return;

        if (!userProfile.getClanId().equals(clanId))
            return;

        if (userProfile.getClanAvatar().equals(clan.getAvatar()))
            return;

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        BrainOutClient.SocialController.sendRequest("follow_clan_avatar",
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();

                Avatars.Clear(userProfile.getClanAvatar());

                String url = response.optString("url", "");
                userProfile.setClan(userProfile.getClanId(), url);

                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
            }
        });
    }

    private void renderLeftButtons()
    {
        leftButtons.clear();

        if (!BrainOutClient.ClientController.isLobby())
            return;

        if (!BrainOut.OnlineEnabled())
            return;

        boolean participating = isParticipatingInConflict();

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        String myAccount = BrainOutClient.ClientController.getMyAccount();

        if (participating)
        {
            {
                Button btn = new Button(BrainOutClient.Skin, "button-notext");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pushMenu(new ConfirmationPopup(L.get("MENU_LEAVE_TITLE"))
                        {
                            @Override
                            public String buttonStyleYes()
                            {
                                return "button-danger";
                            }

                            @Override
                            public void yes()
                            {
                                leaveConflict();
                            }

                            @Override
                            protected boolean reverseOrder()
                            {
                                return true;
                            }
                        });
                    }
                });

                Image image = new Image(BrainOutClient.getRegion("icon-clan-leave"));
                image.setScaling(Scaling.none);
                btn.add(image).expand().fill();

                Label title = new Label(L.get("MENU_LEAVE_DESERT"), BrainOutClient.Skin, "title-small");
                title.setAlignment(Align.center);

                leftButtons.add(btn).size(64, 64).padRight(10);
                leftButtons.add(title).fillX().left().row();
            }

        }
        else
        {
            if (conflict != null)
            {
                Clan.ClanMember me = getMe();

                if (me != null && me.hasPermission(Clan.Permissions.ENGAGE_CONFLICT))
                {
                    Button btn = new Button(BrainOutClient.Skin, "button-notext");

                    btn.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);

                            pushMenu(new RichConfirmationPopup(L.get("MENU_CLAN_CHALLENGE_CANCEL_CONFIRM"))
                            {
                                @Override
                                public String buttonStyleYes()
                                {
                                    return "button-danger";
                                }

                                @Override
                                public void yes()
                                {
                                    cancelConflict();
                                }

                                @Override
                                protected boolean reverseOrder()
                                {
                                    return true;
                                }
                            });
                        }
                    });

                    Image image = new Image(BrainOutClient.getRegion("icon-clan-leave"));
                    image.setScaling(Scaling.none);
                    btn.add(image).expand().fill();

                    Label title = new Label(L.get("MENU_CLAN_CHALLENGE_CANCEL"), BrainOutClient.Skin, "title-small");
                    title.setAlignment(Align.center);

                    leftButtons.add(btn).size(64, 64).padRight(10);
                    leftButtons.add(title).fillX().left().row();
                }

            }

            if (clan.getMembers().containsKey(myAccount) &&
                    (clan.getOwner() == null || !clan.getOwner().getAccountId().equals(myAccount)))
            {
                Button btn = new Button(BrainOutClient.Skin, "button-notext");

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pushMenu(new ConfirmationPopup(L.get("MENU_CLAN_LEAVE_CONFIRM"))
                        {
                            @Override
                            public String buttonStyleYes()
                            {
                                return "button-danger";
                            }

                            @Override
                            public void yes()
                            {
                                leaveClan();
                            }

                            @Override
                            protected boolean reverseOrder()
                            {
                                return true;
                            }
                        });
                    }
                });

                Image image = new Image(BrainOutClient.getRegion("icon-clan-leave"));
                image.setScaling(Scaling.none);
                btn.add(image).expand().fill();

                Label title = new Label(L.get("MENU_CLAN_LEAVE"), BrainOutClient.Skin, "title-small");
                title.setAlignment(Align.center);

                leftButtons.add(btn).size(64, 64).padRight(10);
                leftButtons.add(title).fillX().left().row();
            }

            if (userProfile.isParticipatingClan())
            {
                String accountId = BrainOutClient.ClientController.getMyAccount();
                Clan.ClanMember me = clan.getMembers().get(accountId);

                if (me != null)
                {
                    if (me.hasPermission(Clan.Permissions.CHANGE_SUMMARY))
                    {
                        addEditClanButton();
                    }

                    addDonateButton();
                }
            } else
            {
                if (isClansUnlocked())
                {
                    switch (clan.getJoinMethod())
                    {
                        case free:
                        {
                            addFreeJoinButton();

                            break;
                        }
                        case approve:
                        {
                            if (!BrainOutClient.SocialController.hasOutgoingClanRequest(clanId))
                            {
                                addApproveJoinButton();
                            }

                            break;
                        }
                    }
                }
            }

            Clan myClan = BrainOutClient.SocialController.getMyClan();

            if (myClan != null && !myClan.getId().equals(clan.getId()) && !clan.isInConflict() &&
                    !BrainOutClient.SocialController.hasOutgoingClanEngagement(clanId))
            {
                Clan.ClanMember myMember = myClan.getMembers().get(myAccount);

                if (myMember != null && myMember.hasPermission(Clan.Permissions.ENGAGE_CONFLICT) &&
                        !myClan.isInConflict())
                {
                    addEngageConflictButton();
                }
            }
        }
    }

    private void cancelConflict()
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();

        BrainOutClient.SocialController.sendRequest("cancel_clan_conflict", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private void leaveConflict()
    {
        if (conflictSession == null)
            return;

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        conflictSession.leave(new JsonRPC.ResponseHandler()
        {
            @Override
            public void success(Object response)
            {
                Gdx.app.postRunnable(waitLoadingMenu::pop);
            }

            @Override
            public void error(int code, String message, String data)
            {
                Gdx.app.postRunnable(() ->
                {
                    waitLoadingMenu.pop();
                    pushMenu(new AlertPopup(L.get(message)));
                });
            }
        });
    }

    private void addEngageConflictButton()
    {
        Button btn = new Button(BrainOutClient.Skin, "button-red");

        btn.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                RoomSettings settings = new RoomSettings();
                settings.init(BrainOutClient.ClientController.getUserProfile(), false);

                pushMenu(new EngageConflictMenu(new QuickPlayOptionsMenu.Callback()
                {
                    @Override
                    public void selected(String name, RoomSettings settings, QuickPlayOptionsMenu menu)
                    {
                        engageConflict(settings, ((EngageConflictMenu) menu).getConflictSize());
                    }

                    @Override
                    public void cancelled()
                    {
                        //
                    }
                }, settings));
            }
        });

        Image image = new Image(BrainOutClient.getRegion("icon-clan-engage-conflict"));
        image.setScaling(Scaling.none);
        btn.add(image).expand().fill();

        Label title = new Label(L.get("MENU_CLAN_CHALLENGE_SEND"), BrainOutClient.Skin, "title-small");
        title.setAlignment(Align.center);

        leftButtons.add(btn).size(64, 64).padRight(10);
        leftButtons.add(title).left().row();
    }

    private void engageConflict(RoomSettings settings, int conflictSize)
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        GameService.RoomSettings roomSettings = new GameService.RoomSettings();
        settings.write(roomSettings);

        JSONObject args = new JSONObject();
        args.put("clan_id", clanId);
        args.put("room_settings", roomSettings.getSettings());
        args.put("conflict_size", conflictSize);

        BrainOutClient.SocialController.sendRequest("engage_clan_conflict", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                BrainOutClient.SocialController.addOutgoingClanEngagement(clanId);

                waitLoadingMenu.pop();
                ClanMenu.this.reset();

                pushMenu(new RichAlertPopup(L.get("MENU_CLAN_CHALLENGE_SEND"), L.get("MENU_CLAN_CHALLENGE_SENT")));
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private boolean isClansUnlocked()
    {
        UserProfile profile = BrainOutClient.ClientController.getUserProfile();

        if (profile == null)
            return false;

        OwnableContent clanPass = BrainOutClient.ContentMgr.get(
                Constants.Other.CLAN_PASS,
                OwnableContent.class);

        return clanPass.getLockItem() == null || clanPass.getLockItem().isUnlocked(profile);
    }

    private void addDonateButton()
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();
        int have = (int)(float)userProfile.getStats().get(Constants.User.NUCLEAR_MATERIAL, 0.0f);

        if (have <= 0)
            return;

        Button btn = new Button(BrainOutClient.Skin, "button-notext");

        btn.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                pushMenu(new SendResourcesToClanMenu(have, new SendResourcesToClanMenu.Callback()
                {
                    @Override
                    public void approve(int amount)
                    {
                        donate(amount);
                    }

                    @Override
                    public void cancel()
                    {
                        //
                    }
                }));
            }
        });

        Image image = new Image(BrainOutClient.getRegion("icon-clan-donate"));
        image.setScaling(Scaling.none);
        btn.add(image).expand().fill();

        Label title = new Label(L.get("MENU_CLAN_DONATE"), BrainOutClient.Skin, "title-small");
        title.setAlignment(Align.center);

        leftButtons.add(btn).size(64, 64).padRight(10);
        leftButtons.add(title).left().row();
    }

    private void donate(int amount)
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();
        int have = (int)(float)userProfile.getStats().get(Constants.User.NUCLEAR_MATERIAL, 0.0f);

        if (have < amount)
            return;

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("amount", amount);

        BrainOutClient.SocialController.sendRequest("donate_clan", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private void addEditClanButton()
    {
        Button btn = new Button(BrainOutClient.Skin, "button-notext");

        btn.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);
                pushMenu(new UpdateClanSummaryMenu(clan, ClanMenu.this::reset));
            }
        });

        Image image = new Image(BrainOutClient.getRegion("icon-edit"));
        image.setScaling(Scaling.none);
        btn.add(image).expand().fill();

        Label title = new Label(L.get("MENU_EDIT"), BrainOutClient.Skin, "title-small");
        title.setAlignment(Align.center);

        leftButtons.add(btn).size(64, 64).padRight(10);
        leftButtons.add(title).left().row();
    }

    private void addFreeJoinButton()
    {
        Button btn = new Button(BrainOutClient.Skin, "button-notext");

        btn.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                pushMenu(new JoinClanMenu(clan.getName(), clan.getAvatar(), ClanMenu.this::joinClan));
            }
        });

        Image image = new Image(BrainOutClient.getRegion("icon-add"));
        image.setScaling(Scaling.none);
        btn.add(image).expand().fill();

        Label title = new Label(L.get("MENU_CLAN_JOIN"), BrainOutClient.Skin, "title-small");
        title.setAlignment(Align.center);

        leftButtons.add(btn).size(64, 64).padRight(10);
        leftButtons.add(title).left().row();
    }

    private void addApproveJoinButton()
    {
        Button btn = new Button(BrainOutClient.Skin, "button-notext");

        btn.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                pushMenu(new JoinClanMenu(clan.getName(), clan.getAvatar(), ClanMenu.this::requestJoinClan));
            }
        });

        Image image = new Image(BrainOutClient.getRegion("icon-add"));
        image.setScaling(Scaling.none);
        btn.add(image).expand().fill();

        Label title = new Label(L.get("MENU_CLAN_JOIN"), BrainOutClient.Skin, "title-small");
        title.setAlignment(Align.center);

        leftButtons.add(btn).size(64, 64).padRight(10);
        leftButtons.add(title).left().row();
    }

    private void joinClan()
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("clan_id", clanId);

        BrainOutClient.SocialController.sendRequest("join_clan", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private void requestJoinClan()
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("clan_id", clanId);

        BrainOutClient.SocialController.sendRequest("request_join_clan", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                BrainOutClient.SocialController.addOutgoingClanRequest(clanId);
                waitLoadingMenu.pop();

                ClanMenu.this.reset();
                pushMenu(new RichAlertPopup(L.get("MENU_CLAN_JOIN"), L.get("MENU_CLAN_REQUEST_SENT")));
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private class ChangeDescriptionPopup extends YesNoInputPopup
    {
        public ChangeDescriptionPopup(String text, String value)
        {
            super(text, value);
        }

        @Override
        public void ok()
        {
            changeClanDescription(getValue());
        }

        @Override
        protected TextField newEdit(String value)
        {
            TextArea textArea = new TextArea(value, BrainOutClient.Skin, "edit-default")
            {
                @Override
                protected void initialize()
                {
                    addCaptureListener(new InputListener()
                    {
                        @Override
                        public boolean keyTyped (InputEvent event, char character)
                        {
                            if (character == CARRIAGE_RETURN)
                            {
                                event.stop();
                                return true;
                            }

                            return super.keyTyped(event, character);
                        }
                    });

                    super.initialize();
                }
            };

            textArea.setPrefRows(3);
            textArea.setMaxLength(100);

            return textArea;
        }

        @Override
        protected float getInputWidth()
        {
            return 400;
        }

        @Override
        protected float getInputHeight()
        {
            return 90;
        }
    }

    private void changeClanDescription(String description)
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("description", description);

        BrainOutClient.SocialController.sendRequest("change_clan_description", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private void leaveClan()
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        BrainOutClient.SocialController.sendRequest("leave_clan",
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                ClanMenu.this.close();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get("MENU_ONLINE_ERROR", L.get(reason))));
            }
        });
    }

    private void renderContents(Table contents)
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();
        Clan.ClanMember me = getMe();

        Clan myClan = BrainOutClient.SocialController.getMyClan();

        // avatar
        {
            Table avatarHolder = new Table();

            Image image = new Image(BrainOutClient.getRegion("default-avatar"));
            image.setBounds(4, 4, 120, 120);
            image.setTouchable(Touchable.disabled);

            if (me != null && me.hasPermission(Clan.Permissions.CHANGE_SUMMARY))
            {
                Button avatar = new Button(BrainOutClient.Skin, "button-hoverable");

                avatar.addActor(image);

                Label change = new Label(L.get("MENU_CHANGE"), BrainOutClient.Skin, "title-yellow");
                change.setVisible(false);
                change.setFillParent(true);
                change.setTouchable(Touchable.disabled);
                change.setAlignment(Align.center, Align.center);
                avatar.addActor(change);

                avatar.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        pushMenu(new ChangeAvatarMenu(clan.getAvatarKey() + ".png", url -> changeAvatar(url)));
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                    {
                        change.setVisible(false);
                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                    {
                        change.setVisible(true);
                    }
                });

                avatarHolder.add(avatar).pad(10).size(128, 128);
            }
            else
            {
                avatarHolder.add(image).pad(10).size(128, 128);
            }

            fetchAvatar(clan.getAvatar(), image);

            if (conflict != null)
            {
                {
                    Image vs = new Image(BrainOutClient.Skin, "icon-vs");
                    avatarHolder.add(vs).pad(4);
                }

                Image versus = new Image();
                fetchAvatar(conflict.getAvatar(), versus);

                avatarHolder.add(versus).pad(10).size(128, 128);
            }

            contents.add(avatarHolder).row();
        }

        // nickname
        {
            if (conflict != null)
            {
                Table advancedNickname = new Table();

                Label a = new Label(clan.getName(), BrainOutClient.Skin, "title-yellow");
                Label vs = new Label(L.get("MENU_CLAN_CHALLENGE_VERSUS"), BrainOutClient.Skin, "title-small");
                Label b = new Label(conflict.getName(), BrainOutClient.Skin, "title-yellow");

                advancedNickname.add(a).pad(4);
                advancedNickname.add(vs).pad(4);
                advancedNickname.add(b).pad(4);

                contents.add(advancedNickname).row();

                contents.add(new Label(L.get("MENU_CLAN_CHALLENGED"), BrainOutClient.Skin, "title-gray"))
                    .padBottom(8).row();
            }
            else
            {
                Label nickname = new Label(clan.getName(), BrainOutClient.Skin, "title-small");
                contents.add(nickname).pad(10).padBottom(0).row();
            }
        }

        columnWidth = 620;

        if (conflict == null)
        {
            // account id
            {
                Label id = new Label("id " + String.valueOf(clanId), BrainOutClient.Skin, "title-gray");
                contents.add(id).pad(10).padTop(0).row();
            }

            if (!(userProfile.isParticipatingClan() && userProfile.getClanId().equals(clanId)))
            {
                String loc = null;

                switch (clan.getJoinMethod())
                {
                    case invite:
                    {
                        loc = L.get("MENU_CLAN_JOIN_METHOD_INVITE");
                        break;
                    }
                    case approve:
                    {
                        loc = L.get("MENU_CLAN_JOIN_METHOD_APPROVE");
                        break;
                    }
                }

                if (loc != null)
                {
                    Label nickname = new Label(loc, BrainOutClient.Skin, "title-gray");
                    contents.add(nickname).pad(10).padTop(0).row();
                }
            }

        }
        else
        {
            Table conflictRoot = new Table();

            {
                Table header = new Table(BrainOutClient.Skin);
                header.setBackground("form-red");

                Label label = new Label(L.get("MENU_CLAN_CHALLENGE_PARTICIPANTS"), BrainOutClient.Skin, "title-yellow");
                label.setAlignment(Align.center);
                header.add(label).expandX().fillX().row();

                conflictRoot.add(header).expandX().fillX().row();
            }

            // conflict information
            conflictTable = new Table(BrainOutClient.Skin);
            conflictRoot.add(conflictTable).expandX().fillX().row();

            contents.add(conflictRoot).width(columnWidth).pad(8).padBottom(32).row();
            // conflict chat

            conflictChat = new Table(BrainOutClient.Skin);
            conflictRoot.add(conflictChat).expandX().fillX().padTop(8).row();
            renderConflictChat();

            renderConflictLoading();
        }

        // summary
        {
            Table summary = new Table(BrainOutClient.Skin);
            summary.setBackground("transparent-shape");

            int kills = (int)(float)(clan.getStats().get(Constants.Stats.KILLS, 0.0f));
            int deaths = (int)(float)(clan.getStats().get(Constants.Stats.DEATHS, 0.0f));
            int gamesWon = (int)(float)(clan.getStats().get(Constants.Stats.TOURNAMENTS_WON, 0.0f));
            int nuclearMaterial = (int)(float)(clan.getStats().get(Constants.User.NUCLEAR_MATERIAL, 0.0f));

            float efficiency = deaths != 0 ? (float)kills / (float)deaths : 0;

            addSummaryItem(summary, "icon-clan-members", "MENU_CLAN_STAT_MEMBERS",
                String.valueOf(clan.getMembers().size));
            addSummaryItem(summary, "icon-clan-stats", "MENU_CLAN_STAT_KD",
                StringFunctions.format(efficiency));
            addSummaryItem(summary, "icon-clan-rating", "MENU_STATS_GAMES_WON",
                StringFunctions.format(gamesWon));
            addSummaryItem(summary, "icon-clan-resources", "MENU_CLAN_STAT_RESOURCES",
                String.valueOf(nuclearMaterial));

            contents.add(summary).width(columnWidth).expandX().fillX().row();
        }

        int lcnt = 0;

        // lieutenants
        {
            Array<Clan.ClanMember> lieutenants = new Array<>();

            for (ObjectMap.Entry<String, Clan.ClanMember> entry : clan.getMembers())
            {
                if (entry.value.isLieutenant())
                {
                    lieutenants.add(entry.value);
                }
            }

            lieutenants.sort((o1, o2) -> o2.getRole() - o1.getRole());

            for (Clan.ClanMember member : lieutenants)
            {
                Button btn = new Button(BrainOutClient.Skin,
                    lcnt % 2 == 0 ? "button-row-dark-blue" : "button-row-border-blue");

                String avatar = member.getAvatar();
                Image image;

                if (avatar != null && !avatar.isEmpty())
                {
                    image = new Image();
                    image.setTouchable(Touchable.disabled);

                    Avatars.Get(avatar, (has, avatar1) ->
                    {
                        if (has)
                        {
                            image.setDrawable(new TextureRegionDrawable(new TextureRegion(avatar1)));
                        }
                        else
                        {
                            image.setDrawable(new TextureRegionDrawable(BrainOutClient.getRegion("default-avatar")));
                        }
                    });
                }
                else
                {
                    image = new Image(BrainOutClient.Skin, "default-avatar");
                }

                image.setTouchable(Touchable.disabled);

                btn.add(image).size(48, 48).padRight(8).padLeft(4);

                {
                    Label name = new Label(member.getName(), BrainOutClient.Skin, "title-yellow");
                    name.setTouchable(Touchable.disabled);
                    btn.add(name).padRight(8).expand().left();
                }

                String role;

                if (member == clan.getOwner())
                {
                    role = L.get("MENU_CLAN_LEADER");
                }
                else
                {
                    role = L.get("MENU_ROLE_LIEUTENANT");
                }

                Label roleText = new Label(role, BrainOutClient.Skin, "title-small");
                roleText.setTouchable(Touchable.disabled);
                btn.add(roleText).padRight(8).expand().right();

                btn.addListener(new ClickListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        if (event.isStopped())
                            return;

                        Menu.playSound(MenuSound.select);

                        if (!BrainOut.OnlineEnabled())
                            return;

                        popMeAndPushMenu(new RemoteAccountMenu(member.getAccountId(), member.getCredential()));
                    }
                });

                Table controls = new Table();
                controls.align(Align.right | Align.center);
                controls.setFillParent(true);
                controls.setTouchable(Touchable.childrenOnly);
                controls.setVisible(false);

                boolean haveControls = false;

                if (me != null)
                {
                    if (BrainOutClient.ClientController.isLobby())
                    {
                        if (me.isOwner() && member != me)
                        {
                            {
                                haveControls = true;

                                Button sentToPlayer = new Button(BrainOutClient.Skin, "button-notext");

                                Image img = new Image(BrainOutClient.Skin, "icon-ownership");
                                img.setScaling(Scaling.none);
                                sentToPlayer.add(img);

                                sentToPlayer.addListener(new ClickOverListener()
                                {
                                    @Override
                                    public void clicked(InputEvent event, float x, float y)
                                    {
                                        Menu.playSound(MenuSound.select);
                                        event.stop();

                                        pushMenu(new RichConfirmationPopup(
                                            L.get("MENU_ROLE_TRANFSFER_OWNERSHIP_CONFIRM", member.getName()))
                                        {
                                            @Override
                                            public String buttonStyleYes()
                                            {
                                                return "button-danger";
                                            }

                                            @Override
                                            public void yes()
                                            {
                                                transferOwnership(member);
                                            }

                                            @Override
                                            protected boolean reverseOrder()
                                            {
                                                return true;
                                            }
                                        });
                                    }
                                });

                                controls.add(sentToPlayer).size(48).padRight(6);
                            }

                            {

                                Button sentToPlayer = new Button(BrainOutClient.Skin, "button-notext");

                                Image img = new Image(BrainOutClient.Skin, "icon-lieutenant");
                                img.setScaling(Scaling.none);
                                sentToPlayer.add(img);

                                sentToPlayer.addListener(new ClickOverListener()
                                {
                                    @Override
                                    public void clicked(InputEvent event, float x, float y)
                                    {
                                        Menu.playSound(MenuSound.select);
                                        event.stop();

                                        pushMenu(new ClanPermissionsMenu(member.getPermissions(),
                                                permissions -> changeMemberPermissions(member, permissions)));
                                    }
                                });

                                controls.add(sentToPlayer).size(48).padRight(6);
                            }
                        }
                    }
                }

                boolean haveControls_ = haveControls;

                btn.addActor(controls);

                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                    {
                        if (!haveControls_)
                            return;

                        roleText.setVisible(false);
                        controls.setVisible(true);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                    {
                        if (!haveControls_)
                            return;

                        controls.setVisible(false);
                        roleText.setVisible(true);
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        if (event.isStopped())
                            return;

                        Menu.playSound(MenuSound.select);

                        if (!BrainOut.OnlineEnabled())
                            return;

                        popMeAndPushMenu(new RemoteAccountMenu(member.getAccountId(), member.getCredential()));
                    }
                });

                contents.add(btn).width(columnWidth).height(64).row();

                lcnt++;
            }
        }

        // description
        {
            boolean canEdit = me != null && me.hasPermission(Clan.Permissions.CHANGE_SUMMARY);

            if (clan.hasDescription())
            {
                Table descriptionRoot = new Table(BrainOutClient.Skin);
                descriptionRoot.setBackground(lcnt % 2 == 0 ? "form-dark-blue" : "border-dark-blue");

                Label description = new Label(clan.getDescription(), BrainOutClient.Skin, "title-small");
                description.setWrap(true);
                description.setAlignment(Align.center);

                if (canEdit)
                {
                    Button edit = new Button(BrainOutClient.Skin, "button-hoverable-clear");

                    Image image = new Image(BrainOutClient.getRegion("icon-edit"));
                    image.setScaling(Scaling.fill);
                    edit.add(image).expand().fill();

                    edit.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);
                            pushMenu(new ChangeDescriptionPopup(L.get("MENU_EDIT"), clan.getDescription()));
                        }
                    });

                    descriptionRoot.add(edit).size(30, 30).pad(4).padRight(0);
                }

                descriptionRoot.add(description).pad(16).expandX().fillX().maxHeight(64).row();


                contents.add(descriptionRoot).width(columnWidth).expandX().fillX().row();
            }
            else
            {
                if (canEdit)
                {
                    Table descriptionRoot = new Table(BrainOutClient.Skin);
                    descriptionRoot.setBackground(lcnt % 2 == 0 ? "form-dark-blue" : "border-dark-blue");

                    Button addDescription = new Button(BrainOutClient.Skin, "button-hoverable-clear");

                    Label title = new Label(L.get("MENU_ADD_DESCRIPTION"), BrainOutClient.Skin, "title-gray");
                    addDescription.add(title);

                    addDescription.addListener(new ClickOverListener()
                    {
                        @Override
                        public void clicked(InputEvent event, float x, float y)
                        {
                            Menu.playSound(MenuSound.select);
                            pushMenu(new ChangeDescriptionPopup(L.get("MENU_ADD_DESCRIPTION"), ""));
                        }
                    });

                    descriptionRoot.add(addDescription).pad(16).row();

                    contents.add(descriptionRoot).width(columnWidth).expandX().fillX().row();
                }
            }
        }

        // personnel
        {
            ClientEvent lastTournament = inTournament ? getLastTournament() : null;

            {
                Label title = new Label(L.get("MENU_CLAN_PERSONNEL"), BrainOutClient.Skin, "title-yellow");
                contents.add(title).center().pad(8).padTop(32).row();
            }

            {
                Table header = new Table(BrainOutClient.Skin);
                header.setBackground("border-dark-blue");

                ButtonGroup<Button> buttons = new ButtonGroup<>();
                buttons.setMinCheckCount(1);
                buttons.setMaxCheckCount(1);

                if (lastTournament != null)
                {
                    addSortingButton(header, buttons, "stats-games-won", "MENU_ORDER_BY_CURRENT_TOURNAMENT",
                        new SortingFunction()
                    {
                        @Override
                        public float get(Clan.ClanMember member)
                        {
                            return currentTournament.get(member.getAccountId(), 0.0f);
                        }
                    });
                }

                addSortingButton(header, buttons, "stats-kpd", "MENU_ORDER_BY_KD",
                    new SortingFunction()
                {
                    @Override
                    public float get(Clan.ClanMember member)
                    {
                        return getKD(member);
                    }
                });

                addSortingButton(header, buttons, "icon-nuclear-material-small", "MENU_ORDER_BY_DONATED",
                    new SortingFunction()
                {
                    @Override
                    public float get(Clan.ClanMember member)
                    {
                        return member.getStats().get(Constants.Stats.DONATED, 0.0f);
                    }

                    @Override
                    public String render(Clan.ClanMember member)
                    {
                        return StringFunctions.format(member.getStats().get(Constants.Stats.DONATED, 0.0f)) +
                            " / " + StringFunctions.format(member.getStats().get(
                            Constants.Stats.RESOURCES_RECEIVED, 0.0f));
                    }
                });


                addSortingButton(header, buttons, "stats-kills", "MENU_ORDER_BY_KILLS",
                    new SortingFunction()
                {
                    @Override
                    public float get(Clan.ClanMember member)
                    {
                        return member.getStats().get(Constants.Stats.KILLS, 0.0f);
                    }
                });

                addSortingButton(header, buttons, "stats-deaths", "MENU_ORDER_BY_DEATH",
                    new SortingFunction()
                {
                    @Override
                    public float get(Clan.ClanMember member)
                    {
                        return member.getStats().get(Constants.Stats.DEATHS, 0.0f);
                    }
                });

                if (lastTournament != null)
                {
                    this.sortingTitle = new Label(
                            L.get("MENU_ORDER_BY_CURRENT_TOURNAMENT"), BrainOutClient.Skin, "title-yellow");
                }
                else
                {
                    this.sortingTitle = new Label(L.get("MENU_ORDER_BY_KD"), BrainOutClient.Skin, "title-yellow");
                }

                header.add(sortingTitle).pad(8).expandX().right().row();

                contents.add(header).width(columnWidth).row();
            }

            {
                membersList = new Table(BrainOutClient.Skin);
                contents.add(membersList).width(columnWidth).row();
            }

            if (lastTournament != null)
            {
                sort(new SortingFunction()
                {
                    @Override
                    public float get(Clan.ClanMember member)
                    {
                        return currentTournament.get(member.getAccountId(), 0.0f);
                    }
                });
            }
            else
            {
                sort(new SortingFunction()
                {
                    @Override
                    public float get(Clan.ClanMember member)
                    {
                        return getKD(member);
                    }
                });
            }
        }

        // conflicts history

        if (clan.hasClanHistoryRecords())
        {
            {
                Label title = new Label(L.get("MENU_CLAN_CHALLENGE_HISTORY"), BrainOutClient.Skin, "title-yellow");
                contents.add(title).pad(8).padTop(32).row();
            }

            Table recordsTable = new Table();

            contents.add(recordsTable).width(columnWidth).row();

            int i = 0;

            for (Clan.ClanHistoryRecord record : clan.getClanHistoryRecords())
            {
                Button btn = new Button(BrainOutClient.Skin,
                    i % 2 == 0 ? "button-row-border-blue" : "button-row-dark-blue");

                String avatar = record.getClanAvatar();

                WidgetGroup avatarHolder = new WidgetGroup();

                Image image;

                if (avatar != null && !avatar.isEmpty())
                {
                    image = new Image();
                    image.setTouchable(Touchable.disabled);

                    Avatars.Get(avatar, (has, avatar1) ->
                    {
                        if (has)
                        {
                            image.setDrawable(new TextureRegionDrawable(new TextureRegion(avatar1)));
                        }
                        else
                        {
                            image.setDrawable(new TextureRegionDrawable(BrainOutClient.getRegion("default-avatar")));
                        }
                    });
                }
                else
                {
                    image = new Image(BrainOutClient.Skin, "default-avatar");
                }

                image.setFillParent(true);
                avatarHolder.addActor(image);

                btn.add(avatarHolder).size(48, 48).padRight(8).padLeft(4).padTop(2).padBottom(2);

                {
                    Label name = new Label(record.getClanName(), BrainOutClient.Skin, "title-yellow");
                    name.setTouchable(Touchable.disabled);
                    name.setEllipsis(true);
                    btn.add(name).padRight(8);
                }

                Date date = new Date(record.getTime() * 1000);
                DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
                Label dateTitle = new Label(formatter.format(date), BrainOutClient.Skin, "title-gray");

                dateTitle.setTouchable(Touchable.disabled);
                btn.add(dateTitle).padLeft(32);

                dateTitle.setVisible(false);


                btn.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        if (!BrainOut.OnlineEnabled())
                            return;

                        if (isParticipatingInConflict())
                        {
                            Menu.playSound(MenuSound.denied);
                        }
                        else
                        {
                            Menu.playSound(MenuSound.select);

                            popMeAndPushMenu(new ClanMenu(record.getClanId()));
                        }
                    }

                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                    {
                        dateTitle.setVisible(true);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                    {
                        dateTitle.setVisible(false);
                    }
                });

                String resultStyle;
                String resultTitle;

                switch (record.getResult())
                {
                    case "won":
                    {
                        resultStyle = "title-green";
                        resultTitle = L.get("MENU_WON");
                        break;
                    }
                    case "lost":
                    default:
                    {
                        resultStyle = "title-red";
                        resultTitle = L.get("MENU_LOST");
                        break;
                    }
                }

                {
                    Label result = new Label(resultTitle, BrainOutClient.Skin, resultStyle);
                    btn.add(result).expandX().right().padRight(8);
                }

                recordsTable.add(btn).expandX().fillX().width(columnWidth).row();

                i++;

                if (i >= 8)
                    break;
            }
        }
    }

    private void transferOwnership(Clan.ClanMember member)
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("account_id", member.getAccountId());

        BrainOutClient.SocialController.sendRequest("transfer_ownership", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private void renderConflictChat()
    {
        Table messages = new Table(BrainOutClient.Skin);
        messages.setBackground("chat-text-send");

        conflictChatMessages = new Table()
        {
            @Override
            public void layout()
            {
                super.layout();

                messagesPane.setScrollPercentY(1);
            }
        };

        messagesPane = new ScrollPane(conflictChatMessages, BrainOutClient.Skin, "scroll-default");

        conflictChatMessages.align(Align.top);

        messages.add(messagesPane).height(100).expand().fill().row();

        conflictChat.add(messages).expandX().fill().row();

        Table sendRoot = new Table();

        TextButton infoButton = new TextButton("?", BrainOutClient.Skin, "button-chat-txt");

        infoButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);

                RoomSettings roomSettings = new RoomSettings();
                roomSettings.read(conflictPartyInfo.getSettings());
                roomSettings.init(BrainOutClient.ClientController.getUserProfile(), false);

                pushMenu(new ConflictPreviewMenu(roomSettings, conflictPartyInfo.getMaxMembers()));
            }
        });

        sendRoot.add(infoButton).size(32, 28);

        TextField sendMessage = new TextField("", BrainOutClient.Skin, "edit-chat");

        sendMessage.setTextFieldListener((textField, c) ->
        {
            if (c == 13 || c == 10)
            {
                doSendChat(sendMessage);
            }
        });

        sendRoot.add(sendMessage).expandX().fillX();

        ImageButton sendButton = new ImageButton(BrainOutClient.Skin, "button-chat-send");

        sendButton.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                doSendChat(sendMessage);
            }
        });

        sendRoot.add(sendButton).size(32, 28).row();

        setKeyboardFocus(sendMessage);
        conflictChat.add(sendRoot).expandX().fillX().row();
    }

    private void doSendChat(TextField sendMessage)
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        if (!sendMessage.getText().isEmpty())
        {
            if (conflictSession != null)
            {
                JSONObject msg = new JSONObject();

                msg.put("type", "chat");
                msg.put("sender", userProfile.getName());
                msg.put("clan", userProfile.getClanId());
                msg.put("text", sendMessage.getText());

                conflictSession.sendCustomMessage(msg, new JsonRPC.ResponseHandler()
                {
                    @Override
                    public void success(Object response)
                    {
                        //
                    }

                    @Override
                    public void error(int code, String message, String data)
                    {
                        //
                    }
                });
            }

            sendMessage.setText("");
        }
    }

    private void renderConflictLoading()
    {
        conflictTable.clear();
        conflictTable.add(new LoadingBlock()).pad(48, 80, 80, 48).row();
    }

    private void renderConflictStarting()
    {
        conflictTable.clear();
        Label starting = new Label(L.get("MENU_SELECTING_TEAM"), BrainOutClient.Skin, "title-gray");
        conflictTable.add(starting).pad(64, 64, 64, 64).row();
    }

    private int renderConflictColumn(Table column, Clan clan, Color nameColor)
    {
        int count = 0;
        float levelScale = 0.25f;

        Levels levels = BrainOutClient.ClientController.getLevels(Constants.User.LEVEL);

        for (ObjectMap.Entry<String, GameService.PartyMember> entry : conflictPartyMembers)
        {
            GameService.PartyMember member = entry.value;

            String clanId = member.getProfile().optString("clan-id");

            if (!clan.getId().equals(clanId))
                continue;

            String name = member.getProfile().optString("name", "???");
            String avatar = member.getProfile().optString("avatar");
            int level = member.getProfile().optInt("level", 1);

            Button row = new Button(BrainOutClient.Skin, "button-row-border-blue");

            {
                Image avatarImage = new Image();
                fetchAvatar(avatar, avatarImage);
                row.add(avatarImage).size(60, 60).pad(2);
            }

            {

                Levels.Level level_ = levels.getLevel(level);

                if (level_ != null)
                {
                    Label levelText = new Label(level_.toString(), BrainOutClient.Skin, "player-list");
                    levelText.setColor(nameColor);
                    levelText.setAlignment(Align.left);

                    row.add(levelText).padLeft(4);

                    TextureRegion levelImage = BrainOutClient.getRegion(level_.icon);

                    if (levelImage != null)
                    {
                        Image image = new Image(levelImage);
                        row.add(image).size(levelImage.getRegionWidth() * levelScale,
                                levelImage.getRegionHeight() * levelScale).padLeft(4);
                    }
                }
            }

            {
                Label nameTitle = new Label(name, BrainOutClient.Skin, "title-small");
                nameTitle.setColor(nameColor);
                nameTitle.setTouchable(Touchable.disabled);
                nameTitle.setEllipsis(true);
                row.add(nameTitle).padLeft(4).padRight(8).expandX().fillX();
            }

            column.add(row).expandX().fillX().height(64).row();

            row.addListener(new ClickOverListener()
            {
                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    Menu.playSound(MenuSound.select);

                    if (!BrainOut.OnlineEnabled())
                        return;

                    pushMenu(new RemoteAccountMenu(member.getAccount(), null, true));
                }
            });

            count++;
        }

        return count;
    }

    private boolean isParticipatingInConflict()
    {
        if (!BrainOut.OnlineEnabled())
            return false;

        LoginService loginService = LoginService.Get();

        return conflictPartyMembers != null && loginService != null &&
            conflictPartyMembers.containsKey(BrainOutClient.ClientController.getMyAccount());
    }

    private void renderConflict()
    {
        conflictTable.clear();

        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        boolean participating = isParticipatingInConflict();

        updateClanMenuControls();
        renderLeftButtons();

        int perTeam = conflictPartyInfo.getMaxMembers() / 2;

        // my team
        {
            Table column = new Table();

            int rendered = renderConflictColumn(column, clan, ClientConstants.Menu.KillList.CLAN_COLOR);
            conflictTable.add(column).expandX().uniformX().fillX();

            if (!participating && rendered < perTeam)
            {
                rendered++;

                Button row = new Button(BrainOutClient.Skin, "button-row-border-blue");

                {
                    Label title = new Label(L.get("MENU_CLAN_CHALLENGE_PARTICIPATE"),
                            BrainOutClient.Skin, "title-yellow");

                    row.add(title);
                }

                row.addListener(new ClickOverListener()
                {
                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        Menu.playSound(MenuSound.select);

                        JSONObject profile = new JSONObject();

                        {
                            profile.put("name", userProfile.getName());
                            profile.put("clan-id", clanId);
                            profile.put("level", userProfile.getLevel(Constants.User.LEVEL));

                            if (userProfile.getAvatar() != null && !userProfile.getAvatar().isEmpty())
                            {
                                profile.put("avatar", userProfile.getAvatar());
                            }
                        }

                        JSONObject membersCheck = new JSONObject();

                        {
                            JSONObject child = new JSONObject();

                            {
                                child.put("@func", "num_child_where");
                                child.put("@test", "==");
                                child.put("@field", "clan-id");
                                child.put("@cond", clanId);
                            }

                            JSONObject func = new JSONObject();

                            func.put("@func", "<");
                            func.put("@cond", perTeam);
                            func.put("@value", child);

                            membersCheck.put("members", func);
                        }

                        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
                        pushMenu(waitLoadingMenu);

                        conflictSession.join(profile, membersCheck, new JsonRPC.ResponseHandler()
                        {
                            @Override
                            public void success(Object response)
                            {
                                Gdx.app.postRunnable(waitLoadingMenu::pop);
                            }

                            @Override
                            public void error(int code, String message, String data)
                            {
                                Gdx.app.postRunnable(() ->
                                {
                                    waitLoadingMenu.pop();

                                    switch (code)
                                    {
                                        case 409:
                                        {
                                            pushMenu(new RichAlertPopup(
                                                L.get("MENU_CLAN_CHALLENGE_PARTICIPATE"),
                                                L.get("MENU_TEAM_IS_FULL")
                                            ));

                                            break;
                                        }
                                        default:
                                        {
                                            pushMenu(new RichAlertPopup(
                                                L.get("MENU_CLAN_CHALLENGE_PARTICIPATE"),
                                                message));

                                            break;
                                        }
                                    }

                                });
                            }
                        });
                    }
                });

                column.add(row).expandX().uniformX().fillX().height(64).row();
            }

            for (; rendered < perTeam; rendered++)
            {
                Table row = new Table(BrainOutClient.Skin);
                row.setBackground("border-dark-blue");

                {
                    Label title = new Label(L.get("MENU_CLAN_CHALLENGE_WAITING_FOR_PLAYERS"),
                            BrainOutClient.Skin, "title-gray");

                    row.add(title);
                }

                column.add(row).expandX().uniformX().fillX().height(64).row();
            }
        }

        // enemy team
        {
            Table column = new Table();

            int rendered = renderConflictColumn(column, conflict, ClientConstants.Menu.KillList.ENEMY_COLOR);
            conflictTable.add(column).expandX().fillX().row();

            for (; rendered < perTeam; rendered++)
            {
                Table row = new Table(BrainOutClient.Skin);
                row.setBackground("border-dark-blue");

                {
                    Label title = new Label(L.get("MENU_CLAN_CHALLENGE_WAITING_FOR_PLAYERS"),
                        BrainOutClient.Skin, "title-gray");

                    row.add(title);
                }

                column.add(row).expandX().uniformX().fillX().height(64).row();
            }
        }
    }

    private float getKD(Clan.ClanMember member)
    {
        int kills = (int)(float)(member.getStats().get(Constants.Stats.KILLS, 0.0f));
        int deaths = (int)(float)(member.getStats().get(Constants.Stats.DEATHS, 0.0f));

        return deaths != 0 ? (float)kills / (float)deaths : 0;
    }

    private Clan.ClanMember getMe()
    {
        return clan.getMembers().get(BrainOutClient.ClientController.getMyAccount());
    }

    private abstract class SortingFunction
    {
        public abstract float get(Clan.ClanMember member);
        public String render(Clan.ClanMember member)
        {
            return StringFunctions.format(get(member));
        }
    }

    private void sort(SortingFunction function)
    {
        this.sortingFunction = function;

        renderSorting();
    }

    private void renderSorting()
    {
        membersList.clear();

        Clan.ClanMember me = getMe();

        boolean canKick = BrainOutClient.ClientController.isLobby() && me != null && me.hasPermission("kick");

        Array<Clan.ClanMember> members = new Array<>();

        for (ObjectMap.Entry<String, Clan.ClanMember> entry : clan.getMembers())
        {
            members.add(entry.value);
        }

        members.sort((o1, o2) -> sortingFunction.get(o2) > sortingFunction.get(o1) ? 1 : -1);

        int i = 0;

        for (Clan.ClanMember member : members)
        {
            Button btn = new Button(BrainOutClient.Skin,
                    i % 2 == 0 ? "button-row-dark-blue" : "button-row-border-blue");

            String avatar = member.getAvatar();

            WidgetGroup avatarHolder = new WidgetGroup();

            Image image;

            if (avatar != null && !avatar.isEmpty())
            {
                image = new Image();
                image.setTouchable(Touchable.disabled);

                Avatars.Get(avatar, (has, avatar1) ->
                {
                    if (has)
                    {
                        image.setDrawable(new TextureRegionDrawable(new TextureRegion(avatar1)));
                    }
                    else
                    {
                        image.setDrawable(new TextureRegionDrawable(BrainOutClient.getRegion("default-avatar")));
                    }
                });
            }
            else
            {
                image = new Image(BrainOutClient.Skin, "default-avatar");
            }

            image.setFillParent(true);
            avatarHolder.addActor(image);

            if (canKick && me != null && me.getRole() > member.getRole())
            {
                Image kickImage = new Image(BrainOutClient.Skin, "icon-kick");
                kickImage.setFillParent(true);
                kickImage.setColor(1, 1, 1, 0);

                kickImage.addListener(new ClickListener()
                {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                    {
                        kickImage.setColor(Color.WHITE);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                    {
                        kickImage.setColor(1, 1, 1, 0);
                    }

                    @Override
                    public void clicked(InputEvent event, float x, float y)
                    {
                        event.stop();

                        pushMenu(new ConfirmationPopup(L.get("MENU_CLAN_KICK_CONFIRM", member.getName()))
                        {
                            @Override
                            public void yes()
                            {
                                kickMember(member);
                            }

                            @Override
                            public String buttonStyleYes()
                            {
                                return "button-danger";
                            }

                            @Override
                            protected boolean reverseOrder()
                            {
                                return true;
                            }
                        });
                    }
                });

                avatarHolder.addActor(kickImage);
            }
            else
            {
                image.setTouchable(Touchable.disabled);
            }

            btn.add(avatarHolder).size(48, 48).padRight(8).padLeft(4).padTop(2).padBottom(2);

            Label rank = new Label(String.valueOf(i + 1), BrainOutClient.Skin, "title-small");
            rank.setTouchable(Touchable.disabled);
            btn.add(rank).padRight(8);

            {
                Label name = new Label(member.getName(), BrainOutClient.Skin, "title-yellow");
                name.setTouchable(Touchable.disabled);
                btn.add(name).padRight(8);
            }

            if (playerRecords != null && playerRecords.containsKey(member.getAccountId()))
            {
                List<GameService.PlayerRecord> records = playerRecords.get(member.getAccountId());

                boolean online = false;
                boolean inGame = false;

                for (GameService.PlayerRecord record : records)
                {
                    if (record.getGameServer() == null)
                        continue;

                    if (record.getGameServer().equals("freeplay"))
                    {
                        online = true;
                        continue;
                    }

                    if (record.getGameServer().equals("lobby"))
                    {
                        online = true;
                        continue;
                    }

                    if (record.getGameServer().equals("duel"))
                    {
                        online = true;
                        continue;
                    }

                    {
                        inGame = true;
                        boolean cf = record.getGameServer().equals("clans");

                        TextButton statusInGame = new TextButton(
                                cf ?
                                        L.get("MENU_CLAN_CHALLENGE") :
                                        L.get("MENU_FRIEND_IN_GAME"),
                                BrainOutClient.Skin, "button-status-ingame");


                        statusInGame.addListener(new ClickOverListener()
                        {
                            @Override
                            public void clicked(InputEvent event, float x, float y)
                            {
                                event.stop();

                                Menu.playSound(MenuSound.select);

                                pushMenu(new OtherPlayerPreviewMenu(record, () ->
                                {
                                    pop();

                                    Matchmaking.JoinGame(record.getRoomId(),
                                        new Matchmaking.JoinGameResult()
                                    {
                                        @Override
                                        public void complete(String roomId)
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
                                    });
                                }));
                            }
                        });

                        btn.add(statusInGame).width(128).pad(8);
                    }

                    break;
                }

                if (online && !inGame)
                {
                    Label onlineTitle = new Label(
                        L.get("MENU_FRIEND_ONLINE"), BrainOutClient.Skin, "title-light-blue");
                    onlineTitle.setAlignment(Align.center);

                    btn.add(onlineTitle).width(128).pad(8);
                }
            }

            String value = sortingFunction.render(member);
            Label valueText = new Label(value, BrainOutClient.Skin, "title-small");
            valueText.setTouchable(Touchable.disabled);
            btn.add(valueText).padRight(8).expand().right();

            Table controls = new Table();
            controls.align(Align.right | Align.center);
            controls.setFillParent(true);
            controls.setTouchable(Touchable.childrenOnly);
            controls.setVisible(false);

            boolean haveControls = false;

            if (me != null)
            {
                if (BrainOutClient.ClientController.isLobby())
                {
                    int clanMaterial = (int)(float)clan.getStats().get(Constants.User.NUCLEAR_MATERIAL, 0.0f);

                    if (me.hasPermission(Clan.Permissions.SEND_RESOURCES) && clanMaterial > 0)
                    {
                        {
                            haveControls = true;

                            Button sentToPlayer = new Button(BrainOutClient.Skin, "button-notext");

                            Image img = new Image(BrainOutClient.Skin, "button-player-support");
                            sentToPlayer.add(img);

                            sentToPlayer.addListener(new ClickOverListener()
                            {
                                @Override
                                public void clicked(InputEvent event, float x, float y)
                                {
                                    Menu.playSound(MenuSound.select);
                                    event.stop();

                                    pushMenu(new SendResourcesToClanMemberMenu(
                                        clanMaterial, member.getName(), member.getAvatar(),
                                    new SendResourcesToClanMemberMenu.Callback()
                                    {
                                        @Override
                                        public void approve(int amount)
                                        {
                                            sendResourcesToClanMember(member, amount);
                                        }

                                        @Override
                                        public void cancel()
                                        {

                                        }
                                    }));
                                }
                            });

                            controls.add(sentToPlayer).size(48).padRight(6);
                        }
                    }

                    if (me.isOwner() && member != me)
                    {
                        {
                            haveControls = true;

                            Button sentToPlayer = new Button(BrainOutClient.Skin, "button-notext");

                            Image img = new Image(BrainOutClient.Skin, "icon-lieutenant");
                            img.setScaling(Scaling.none);
                            sentToPlayer.add(img);

                            sentToPlayer.addListener(new ClickOverListener()
                            {
                                @Override
                                public void clicked(InputEvent event, float x, float y)
                                {
                                    Menu.playSound(MenuSound.select);
                                    event.stop();

                                    pushMenu(new ClanPermissionsMenu(member.getPermissions(),
                                        permissions -> changeMemberPermissions(member, permissions)));
                                }
                            });

                            controls.add(sentToPlayer).size(48).padRight(6);
                        }
                    }
                }
            }

            boolean haveControls_ = haveControls;

            btn.addActor(controls);

            btn.addListener(new ClickOverListener()
            {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor)
                {
                    if (!haveControls_)
                        return;

                    valueText.setVisible(false);
                    controls.setVisible(true);
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor)
                {
                    if (!haveControls_)
                        return;

                    controls.setVisible(false);
                    valueText.setVisible(true);
                }

                @Override
                public void clicked(InputEvent event, float x, float y)
                {
                    if (event.isStopped())
                        return;

                    Menu.playSound(MenuSound.select);

                    if (!BrainOut.OnlineEnabled())
                        return;

                    popMeAndPushMenu(new RemoteAccountMenu(member.getAccountId(), member.getCredential()));
                }
            });

            membersList.add(btn).width(columnWidth).height(64).row();

            i++;
        }
    }

    private void changeMemberPermissions(Clan.ClanMember member, Set<String> permissions)
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("account_id", member.getAccountId());

        JSONArray permissions_ = new JSONArray();

        for (String permission : permissions)
        {
            permissions_.put(permission);
        }

        args.put("permissions", permissions_);

        BrainOutClient.SocialController.sendRequest("change_member_permissions", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private void sendResourcesToClanMember(Clan.ClanMember member, int amount)
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("account_id", member.getAccountId());
        args.put("amount", amount);

        BrainOutClient.SocialController.sendRequest("send_resources_to_member", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private void kickMember(Clan.ClanMember member)
    {
        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("account_id", member.getAccountId());
        args.put("name", member.getName());

        BrainOutClient.SocialController.sendRequest("kick_clan_member", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();
                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private void addSortingButton(Table header, ButtonGroup<Button> buttons, String icon, String title,
                                  SortingFunction sort)
    {
        Button btn = new Button(BrainOutClient.Skin, "button-notext-checkable");
        Image image = new Image(BrainOutClient.Skin, icon);
        image.setScaling(Scaling.none);

        btn.add(image).size(16);
        btn.setUserObject(sort);

        btn.addListener(new ClickOverListener()
        {
            @Override
            public void clicked(InputEvent event, float x, float y)
            {
                Menu.playSound(MenuSound.select);
                sortingTitle.setText(L.get(title));
                sort(sort);
            }
        });

        buttons.add(btn);
        header.add(btn).size(32).pad(8);
    }

    private void addSummaryItem(Table summary, String icon, String loc, String value)
    {
        Table column = new Table();

        {
            Image image = new Image(BrainOutClient.Skin, icon);
            image.setScaling(Scaling.none);
            column.add(image).size(72, 56).pad(4).row();
        }

        {
            Label title = new Label(L.get(loc), BrainOutClient.Skin, "title-yellow");
            title.setAlignment(Align.center);
            column.add(title).expandX().center().row();
        }

        {
            Label description = new Label(value, BrainOutClient.Skin, "title-small");
            description.setAlignment(Align.center);
            column.add(description).expandX().center().row();
        }

        summary.add(column).padTop(4).padBottom(8).expandX().uniformX().center();
    }

    private void changeAvatar(String url)
    {
        UserProfile userProfile = BrainOutClient.ClientController.getUserProfile();

        WaitLoadingMenu waitLoadingMenu = new WaitLoadingMenu("");
        pushMenu(waitLoadingMenu);

        JSONObject args = new JSONObject();
        args.put("url", url);

        BrainOutClient.SocialController.sendRequest("change_clan_avatar", args,
            new SocialController.RequestCallback()
        {
            @Override
            public void success(JSONObject response)
            {
                waitLoadingMenu.pop();

                Avatars.Clear(url);

                userProfile.setClan(userProfile.getClanId(), url);

                ClanMenu.this.reset();
            }

            @Override
            public void error(String reason)
            {
                waitLoadingMenu.pop();
                pushMenu(new AlertPopup(L.get(reason)));
            }
        });
    }

    private void fetchAvatar(String url, Image avatar)
    {
        if (url.isEmpty())
        {
            avatar.setDrawable(BrainOutClient.Skin, "default-avatar");
            return;
        }

        Avatars.Get(url, (has, avatarTexture) ->
        {
            if (has)
            {
                avatar.setDrawable(new TextureRegionDrawable(new TextureRegion(avatarTexture)));
            }
            else
            {
                avatar.setDrawable(BrainOutClient.Skin, "default-avatar");
            }
        });
    }

    @Override
    public void onInit()
    {
        super.onInit();

        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);

        fetchClan();

        if (BrainOutClient.ClientController.isLobby())
        {
            addGroupChatButton();
        }
    }

    private void addGroupChatButton()
    {

        TextureRegion icon = BrainOutClient.getRegion("icon-chat");
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

                    LobbyChatWidget.Open("clan");
                }
            });

            chatButton.setBounds(24, 16, 64, 64);

            addActor(chatButton);
        }
    }

    @Override
    public void onRelease()
    {
        super.onRelease();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);

        if (conflictSession != null)
        {
            conflictSession.close();
            conflictSession = null;
        }
    }

    @Override
    public void reset()
    {
        super.reset();

        conflict = null;

        if (conflictPartyMembers != null)
        {
            conflictPartyMembers.clear();
        }

        if (conflictSession != null)
        {
            conflictSession.close();
            conflictSession = null;
        }

        fetchClan();
    }

    private void fetchClan()
    {
        if (!BrainOut.OnlineEnabled())
        {
            SocialService.Group group = new SocialService.Group(new JSONObject("{\"group\": {\"name\": \"Test Clan\", \"owner\": \"1\", \"profile\": {}}, \"participants\": {\"1\": {\"profile\": {\"name\": \"Test Account\"}}}}"));
            processClan(group, false);

            return;
        }

        SocialService socialService = SocialService.Get();
        LoginService loginService = LoginService.Get();

        if (socialService != null && loginService != null)
        {
            socialService.getGroup(loginService.getCurrentAccessToken(), this.clanId,
                 (service, request, result, group) -> Gdx.app.postRunnable(() ->
            {
                if (result == Request.Result.success)
                {
                    Gdx.app.postRunnable(() -> fetchCurrentTournamentParticipation(group));
                }
                else
                {
                    Gdx.app.postRunnable(() -> renderError(L.get("MENU_ONLINE_ERROR", result.toString())));
                }
            }));
        }
    }

    private ClientEvent getLastTournament()
    {
        Array<ClientEvent> events = BrainOutClient.ClientController.getOnlineEvents();

        for (ClientEvent event : events)
        {
            if (!event.getEvent().group)
                continue;

            if (!event.getEvent().isValid())
                continue;

            return event;
        }

        for (ClientEvent event : events)
        {
            if (!event.getEvent().group)
                continue;

            return event;
        }

        return null;
    }

    private void fetchCurrentTournamentParticipation(SocialService.Group group)
    {
        EventService eventService = EventService.Get();
        LoginService loginService = LoginService.Get();

        if (eventService == null || loginService == null)
        {
            processClan(group, false);
            return;
        }

        ClientEvent last = getLastTournament();

        if (last == null)
        {
            processClan(group, false);
            return;
        }

        eventService.getGroupEventParticipants(loginService.getCurrentAccessToken(),
            String.valueOf(last.getEvent().id),
            clanId, (service, request, result, participants) ->
        {
            Gdx.app.postRunnable(() ->
            {
                if (result == Request.Result.success)
                {
                    for (String accountId : participants.keySet())
                    {
                        EventService.GroupEventParticipant participant = participants.get(accountId);
                        ClanMenu.this.currentTournament.put(accountId, participant.score);
                    }

                    processClan(group, !participants.isEmpty());
                }
                else
                {
                    processClan(group, false);
                }
            });
        });
    }

    private void renderError(String errorText)
    {
        data.clear();

        Label wait = new Label(errorText, BrainOutClient.Skin, "title-red");
        wait.setAlignment(Align.center);

        data.add(wait).pad(32).expandX().fillX().center().row();
    }

    @Override
    public boolean escape()
    {
        pop();

        return true;
    }

    public ClanMenu(String clanId)
    {
        this.clanId = clanId;
        this.currentTournament = new ObjectMap<>();
    }

    @Override
    public boolean lockInput()
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
                    case clanInfoUdated:
                    {
                        clanInfoUpdated();

                        break;
                    }
                }

                break;
            }
        }

        return false;
    }

    private void clanInfoUpdated()
    {
        reset();
    }
}
