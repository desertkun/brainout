package com.desertkun.brainout.mode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.RemoteClient;
import com.desertkun.brainout.client.states.CSError;
import com.desertkun.brainout.client.states.CSGame;
import com.desertkun.brainout.client.states.CSGetRegions;
import com.desertkun.brainout.common.msg.SetFriendlyStatusMsg;
import com.desertkun.brainout.common.msg.client.cards.*;
import com.desertkun.brainout.common.msg.server.*;
import com.desertkun.brainout.components.ClientPlayerComponent;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.my.MyPlayerComponent;
import com.desertkun.brainout.content.components.ClientMarketContainerComponent;
import com.desertkun.brainout.content.components.GeigerComponent;
import com.desertkun.brainout.content.components.ItemLimitsComponent;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.quest.DailyQuest;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.content.quest.Tree;
import com.desertkun.brainout.content.quest.task.ClientTask;
import com.desertkun.brainout.content.quest.task.Task;
import com.desertkun.brainout.content.upgrades.ExtendedStorage;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.FreePlayMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.data.interfaces.Watcher;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.gs.ActionPhaseState;
import com.desertkun.brainout.gs.actions.MenuAction;
import com.desertkun.brainout.gs.actions.WaitAction;
import com.desertkun.brainout.menu.impl.*;
import com.desertkun.brainout.menu.impl.realestate.RealEstateActionMenu;
import com.desertkun.brainout.menu.impl.realestate.RealEstateEditMenu;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.online.Matchmaking;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.online.UserProfile;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.posteffects.plain.NoiseEffect;
import com.desertkun.brainout.utils.RealEstateInfo;
import org.json.JSONObject;

import java.util.Objects;

public class ClientFreeRealization extends ClientGameRealization<GameModeFree>
{
    private static PointLaunchData fakeData = new PointLaunchData(0, 0, 0, null);
    private final Map.Predicate radioactivePredicate;
    private Vector2 lastLocation;

    private JSONObject summary;
    private Array<Quest> activeQuests;
    private float cnt = 2;
    private NoiseEffect noise;
    private ConsumableContent geiger;
    private float tick, tickCnt;
    private boolean shaderSupport;

    public ClientFreeRealization(GameModeFree gameMode)
    {
        super(gameMode);

        activeQuests = new Array<>();
        shaderSupport = BrainOutClient.ClientController.getSettings().isShaderEffectsEnabled();
        radioactivePredicate = activeData -> activeData.getComponent(RadioactiveComponentData.class) != null;

        tick = 0;
        tickCnt = 0;
    }

    @Override
    public Color getColorOf(ActiveData data)
    {
        KarmaComponentData kmp = data.getComponent(KarmaComponentData.class);
        if (kmp == null)
            return null;

        int karma = kmp.getKarma();
        return GetKarmaColor(karma);
    }


    private void notifyCardEvent(CardMessage msg)
    {
        BrainOutClient.EventMgr.sendDelayedEvent(FreePlayCardsEvent.obtain(msg));
    }

    @SuppressWarnings("unused")
    public boolean received(final CardsTable msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final DiscardAllCards msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final ResetTable msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final DiscardCard msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final FlipCard msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final LeaveTable msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final JoinCards msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final GiveCardToPlayerFromTable msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final GiveCardToPlayerFromDeck msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final TakeCardOffDeckOntoTable msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final MoveCardOnTable msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PlaceCardOnTableFromHand msg)
    {
        notifyCardEvent(msg);
        return true;
    }

    public static Color GetKarmaColor(int karma)
    {
        if (karma <= -5)
            return ClientConstants.Menu.KillList.KARMA_VERY_BAD;

        if (karma < -1)
            return ClientConstants.Menu.KillList.KARMA_BAD;

        if (karma >= 5)
            return ClientConstants.Menu.KillList.KARMA_VERY_GOOD;

        if (karma > 1)
            return ClientConstants.Menu.KillList.KARMA_GOOD;

        return ClientConstants.Menu.KillList.KARMA_OK;
    }

    public Color getColorOf(RemoteClient remoteClient)
    {
        return GetKarmaColor(remoteClient.getInfoInt("karma", -5));
    }

    @Override
    public void update(float dt)
    {
        cnt -= dt;

        if (cnt < 0)
        {
            cnt = 0.05f;

            updateWave(cnt);
        }

        if (tick > 0)
        {
            tickCnt -= dt;
            if (tickCnt < 0)
            {
                tickCnt = tick * MathUtils.random(0.8f, 1.2f);
                GeigerComponent gc = geiger.getComponent(GeigerComponent.class);
                ClientMap map = Map.GetWatcherMap(ClientMap.class);

                if (gc != null && map != null && Map.GetWatcher() != null)
                {
                    fakeData.setPosition(Map.GetWatcher().getWatchX(), Map.GetWatcher().getWatchY());
                    fakeData.setDimension(Map.GetWatcher().getDimension());
                    gc.getSounds().launchEffects(fakeData);
                }
            }
        }
    }

    @Override
    public Class<? extends ClientMap> getMapClass()
    {
        return FreePlayMap.class;
    }

    private void updateWave(float dt)
    {
        CSGame game = BrainOutClient.ClientController.getState(CSGame.class);

        if (game == null)
            return;

        Watcher watcher = Map.GetWatcher();

        if (watcher == null)
        {
            return;
        }

        Map map = Map.Get(watcher.getDimension());

        if (map == null)
            return;

        PlayerData playerData = game.getPlayerData();

        float x, y;

        if (playerData != null)
        {
            x = playerData.getX();
            y = playerData.getY();
        }
        else
        {
            x = watcher.getWatchX();
            y = watcher.getWatchY();
        }

        if (lastLocation == null)
        {
            lastLocation = new Vector2(x, y);
        }
        else
        {
            lastLocation.set(x, y);
        }

        ActiveData closesSpot = map.getClosestActiveForTag(192, x, y,
                ActiveData.class, Constants.ActiveTags.RADIOACTIVE, radioactivePredicate);

        float depth;

        if (closesSpot == null)
        {
            depth = 0;
        }
        else
        {
            RadioactiveComponentData rad = closesSpot.getComponent(RadioactiveComponentData.class);
            depth = Interpolation.circle.apply(rad.func(x, y, 4));
        }

        if (geiger != null && playerData != null)
        {
            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

            if (poc != null && poc.getConsumableContainer().hasConsumable(geiger))
            {
                //setNoise(depth / 2.0f);

                if (depth <= 0)
                {
                    tick = 0;
                }
                else
                {
                    float dp1 = 1.0f + depth;
                    float dp2 = dp1 * dp1;
                    float dp3 = dp2 * dp2;

                    tick = MathUtils.clamp(1.0f / dp3, 0.06f, 1.0f);
                }
            }
            else
            {
                tick = 0;
                //setNoise(0);
            }
        }
        else
        {
            tick = 0;
            //setNoise(0);
        }
    }

    @Override
    public void postRender()
    {
        super.postRender();

        if (noise != null)
        {
            noise.render();
        }
    }

    public boolean hasParty()
    {
        return BrainOutClient.ClientController.getCurrentPartyId() != null;
    }

    @Override
    public void init(PlayState.InitCallback callback)
    {
        BrainOutClient.MusicMng.stopMusic();

        UserProfile profile = BrainOutClient.ClientController.getUserProfile();

        geiger = BrainOutClient.ContentMgr.get("consumable-item-geiger", ConsumableContent.class);

        BrainOut.ContentMgr.queryContentGen(Tree.class, tree ->
        {
            if (!tree.isActive(profile, BrainOutClient.ClientController.getMyAccount()))
                return;

            Quest current = tree.getCurrentQuest(profile, BrainOutClient.ClientController.getMyAccount());

            if (current == null)
                return;

            if (current.isCoop() && !hasParty())
            {
                return;
            }

            activeQuests.add(current);
        });

        super.init(callback);
    }

    public boolean isQuestActive(Quest quest)
    {
        return activeQuests.contains(quest, true);
    }

    public boolean isItemUseless(ConsumableItem item)
    {
        ItemLimitsComponent limits = item.getContent().getComponent(ItemLimitsComponent.class);
        return limits != null && !limits.getLimits().passes(BrainOutClient.ClientController.getUserProfile());
    }

    public boolean isItemQuestRelated(ConsumableItem item)
    {
        if (item.getContent() instanceof ExtendedStorage)
        {
            return ExtendedStorage.HasRoomToExtend(
                BrainOutClient.ClientController.getUserProfile()) == item.getContent();
        }

        for (Quest quest : activeQuests)
        {
            if (quest instanceof DailyQuest &&
                    ((DailyQuest) quest).isQuestDoneForToday(BrainOutClient.ClientController.getUserProfile()))
                continue;

            for (ObjectMap.Entry<String, Task> entry : quest.getTasks())
            {
                if (!(entry.value instanceof ClientTask))
                    continue;

                ClientTask clientTask = ((ClientTask) entry.value);

                if (clientTask.isItemTaskRelated(item))
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean isEnemies(RemoteClient a, RemoteClient b)
    {
        if (a == b || a == null || b == null)
            return false;

        int ag = a.getInfoInt("friend", -1);
        int bg = b.getInfoInt("friend", -1);

        if (ag != -1 && (ag == bg))
        {
            return false;
        }

        if (a.getPartyId() == null || b.getPartyId() == null)
            return true;

        return !Objects.equals(a.getPartyId(), b.getPartyId());
    }

    @Override
    protected void updateStats()
    {
        updateCurrentState();

        if (stats != null)
        {
            stats.clear();

            if (getGameMode().getPhase() == GameMode.Phase.game)
            {
                stats.add(new Image(BrainOutClient.Skin, "icon-coop-small")).padRight(8);
                Label alive = new Label(String.valueOf(GameModeFree.GetPlayersAlive()), BrainOutClient.Skin,
                        GameModeFree.IsEnoughPlayersToLeave() ? "title-green" : "title-small");
                stats.add(alive);
                renderTimeLeft(stats);
            }
        }
    }

    public void updateCurrentState()
    {
        if (topStats == null)
            return;

        topStats.clear();

        switch (getGameMode().getPhase())
        {
            case game:
            {
                renderPartners();
                break;
            }
        }
    }

    private boolean hasPartner()
    {
        RemoteClient me = BrainOutClient.ClientController.getMyRemoteClient();

        if (me == null)
            return false;

        String myPartyId = me.getPartyId();

        for (ObjectMap.Entry<Integer, RemoteClient> entry : BrainOutClient.ClientController.getRemoteClients())
        {
            if (entry.value == me || entry.value == null)
                continue;

            if (entry.value.isFriend(me))
                return true;

            if (myPartyId != null && myPartyId.equals(entry.value.getInfoString("party", "")))
            {
                return true;
            }
        }

        return false;
    }

    /*
    private void setNoise(float v)
    {
        if (v == 0)
        {
            disposeNoise();

            if (shaderSupport)
            {

                GameState topState = BrainOutClient.getInstance().topState();
                if (topState instanceof ActionPhaseState)
                {
                    ActionPhaseState ap = ((ActionPhaseState) topState);
                    PostEffects postEffects = ap.getPostEffects();

                    if (postEffects.getPostEffect() instanceof GrayScaledPostEffect)
                    {
                        postEffects.resetEffect();
                    }
                }
            }
        }
        else
        {
            if (shaderSupport)
            {
                GameState topState = BrainOutClient.getInstance().topState();
                if (topState instanceof ActionPhaseState)
                {

                    ActionPhaseState ap = ((ActionPhaseState) topState);
                    PostEffects postEffects = ap.getPostEffects();

                    if (postEffects.getPostEffect() instanceof GrayScaledPostEffect)
                    {
                        GrayScaledPostEffect gp = ((GrayScaledPostEffect) postEffects.getPostEffect());
                        gp.setValue(v);
                    } else
                    {

                        GrayScaledPostEffect gp = new GrayScaledPostEffect();
                        gp.setValue(v);
                        ap.getPostEffects().setEffect(gp);
                    }
                }
            }

            if (noise != null)
            {
                float here = v / 5.0f;

                noise.setValue(here);
            }
            else
            {
                noise = new NoiseEffect(v);
            }
        }
    }

     */

    private void renderPartners()
    {
        topStats.clear();

        if (!hasPartner())
            return;

        RemoteClient me = BrainOutClient.ClientController.getMyRemoteClient();

        if (me == null)
            return;

        String myPartyId = me.getPartyId();

        Table stats = new Table(BrainOutClient.Skin);
        stats.setBackground("form-default");
        topStats.add(stats).pad(16).expandX().left().top().row();

        for (ObjectMap.Entry<Integer, RemoteClient> entry : BrainOutClient.ClientController.getRemoteClients())
        {
            RemoteClient remoteClient = entry.value;

            if (remoteClient == me || remoteClient == null)
                continue;

            if (remoteClient.isFriend(me) ||
                (myPartyId != null && myPartyId.equals(remoteClient.getInfoString("party", ""))))
            {
                Table playerEntry = new Table();
                stats.add(playerEntry).expandX().fillX().row();

                {
                    if (!remoteClient.getAvatar().isEmpty())
                    {
                        Table avatarInfo = new Table();

                        fetchAvatar(remoteClient.getAvatar(), avatarInfo);

                        playerEntry.add(avatarInfo);
                    }
                    else
                    {
                        Image def = new Image(BrainOutClient.Skin, "default-avatar");
                        def.setScaling(Scaling.fit);
                        playerEntry.add(def).size(40, 40);
                    }
                }

                Table playerEntryStats = new Table();
                playerEntry.add(playerEntryStats).expandX().fillX().pad(8);

                {
                    String name = remoteClient.getName();

                    if (name.length() > 20)
                    {
                        name = name.substring(0, 20) + "...";
                    }

                    Label clientName = new Label(name, BrainOutClient.Skin, "player-list");
                    clientName.setAlignment(Align.left);
                    clientName.setEllipsis(true);
                    playerEntryStats.add(clientName).expandX().fillX().row();
                }
                {
                    if (entry.value.getInfoBoolean("dead", false))
                    {
                        Label dead = new Label(L.get("MENU_DEAD"), BrainOutClient.Skin, "title-red");
                        dead.setAlignment(Align.left);
                        dead.setEllipsis(true);
                        playerEntryStats.add(dead).expandX().fillX().row();
                    }
                    else
                    {

                        float hp = entry.value.getInfoFloat("hp", 200);

                        if (entry.value.getInfoBoolean("wounded", false))
                        {
                            Label dead = new Label(L.get("MENU_WOUNDED"), BrainOutClient.Skin, "title-yellow");
                            dead.setAlignment(Align.left);
                            dead.setEllipsis(true);
                            playerEntryStats.add(dead).expandX().fillX().row();
                        }
                        else
                        {
                            ProgressBar healthBar = new ProgressBar(0, 200, 1, false, BrainOutClient.Skin,
                                    "progress-health");
                            healthBar.setValue(hp);
                            healthBar.setSize(
                                    ClientConstants.Menu.PlayerInfo.LABEL_WIDTH,
                                    ClientConstants.Menu.PlayerInfo.LABEL_HEIGHT
                            );

                            playerEntryStats.add(healthBar).expandX().fillX().padTop(4).row();
                        }
                    }
                }
            }
        }
    }

    protected void fetchAvatar(String avatar, Table avatarInfo)
    {
        Avatars.Get(avatar, (has, avatarTexture) ->
        {
            if (has)
            {
                Image avatarImage = new Image(avatarTexture);

                avatarImage.setScaling(Scaling.fit);

                avatarInfo.add(avatarImage).size(40, 40).row();
            }
        });
    }

    @Override
    protected void updated()
    {
        updateStats();
    }

    private String getPlayerStats()
    {
        int count = 0;

        for (ObjectMap.Entry<Integer, RemoteClient> client : BrainOutClient.ClientController.getRemoteClients())
        {
            RemoteClient remoteClient = client.value;

            if (remoteClient.getInfoBoolean("bot", false))
                continue;

            count++;
        }

        return String.valueOf(count) + " / " +
                BrainOutClient.ClientController.getMaxPlayers();
    }

    @Override
    public void onKilledBy(ActionPhaseState ps, Map map, ActiveData killer, InstrumentInfo info)
    {
        if (getGameMode().getPhase() == GameMode.Phase.game)
        {
            super.onKilledBy(ps, map, killer, info);
        }
    }

    @Override
    public void init(ActionPhaseMenu menu)
    {
        super.init(menu);

        BrainOutClient.EventMgr.subscribe(Event.ID.freePlaySummary, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.activeAction, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.simple, this);
        BrainOutClient.EventMgr.subscribe(Event.ID.gameController, this);
    }

    private void disposeNoise()
    {
        if (noise != null)
        {
            noise.dispose();
            noise = null;
        }
    }

    @Override
    public void release()
    {
        super.release();

        activeQuests.clear();

        disposeNoise();

        BrainOutClient.EventMgr.unsubscribe(Event.ID.freePlaySummary, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.activeAction, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.simple, this);
        BrainOutClient.EventMgr.unsubscribe(Event.ID.gameController, this);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case simple:
            {
                SimpleEvent e = ((SimpleEvent) event);

                if (e.getAction() == null)
                    return false;

                switch (e.getAction())
                {
                    case playersStatsUpdated:
                    {
                        updateCurrentState();

                        break;
                    }
                }
                break;
            }
            case freePlaySummary:
            {
                FreePlaySummaryEvent ev = ((FreePlaySummaryEvent) event);
                onSummaryReceived(ev.summary, ev.alive);

                break;
            }

            case gameController:
            {
                GameControllerEvent ev = ((GameControllerEvent) event);

                switch (ev.action)
                {
                    case freePlayFriends:
                    {
                        toggleFriendly();
                        return true;
                    }
                }

                return false;
            }

            case activeAction:
            {
                ActiveActionEvent ev = ((ActiveActionEvent) event);

                switch (ev.action)
                {
                    case removed:
                    case added:
                    {
                        if (ev.activeData instanceof PlayerData)
                        {
                            Gdx.app.postRunnable(this::updateStats);
                        }
                        break;
                    }
                }

                break;
            }
        }

        return false;
    }

    @SuppressWarnings("unused")
    public boolean received(final MadeFriendsMsg msg)
    {
        final int with = msg.with;

        Gdx.app.postRunnable(() ->
        {
            RemoteClient remoteClient = BrainOutClient.ClientController.getRemoteClients().get(with);
            if (remoteClient == null)
                return;

            BrainOut.EventMgr.sendDelayedEvent(ChatEvent.obtain(new ChatMsg(
                "system", L.get(msg.friend ? "MENU_FP_MADE_FRIENDS" : "MENU_FP_NO_LONGER_FRIENDS",
                remoteClient.getName()), "", msg.friend ? Color.GREEN : Color.RED, -1
            )));

            BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.teamUpdated));
            BrainOut.EventMgr.sendDelayedEvent(SimpleEvent.obtain(SimpleEvent.Action.playersStatsUpdated));
        });

        return true;
    }

    @SuppressWarnings("unused")
    public boolean received(final PartyStartedMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            JSONObject settings = new JSONObject(msg.settings);
            Matchmaking.Connect(msg.key, msg.host, msg.ports, settings, new Runnable()
            {
                @Override
                public void run()
                {
                    BrainOutClient.ClientController.setState(new CSError(L.get("MENU_FAILED_TO_CONNECT"),
                        () -> BrainOutClient.ClientController.setState(new CSGetRegions())));
                }
            });
        });

        return true;
    }

    public boolean received(final TeamLandingMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            RemoteClient friend = BrainOutClient.ClientController.getRemoteClients().get(msg.clientId);

            if (friend == null) return;

            int teamLandingTimer = (int)(msg.time / 1000 - BrainOutClient.ClientController.getServerTime() + Constants.Other.TEAM_LANDING_TIMER);

            BrainOutClient.EventMgr.sendDelayedEvent(OnScreenMessagesEvent.obtain(
                    "You have %d seconds to spawn near your teammate " + friend.getName(),
                    teamLandingTimer, false, Align.top, "title-yellow",
                    Constants.Other.TEAM_LANDING_ACTION_NAME, true
            ));
        });

        return true;
    }

    public boolean received(final FreeEnterMsg msg)
    {
        Gdx.app.postRunnable(() ->
        {
            MenuAction currentAction = BrainOutClient.Actions.getCurrentAction();
            if (currentAction != null && Constants.Other.TEAM_LANDING_ACTION_NAME.equals(currentAction.getName()))
            {
                currentAction.done();
            }
        });
        return true;
    }

    @Override
    public boolean forcePlayerList()
    {
        RemoteClient me = BrainOutClient.ClientController.getMyRemoteClient();

        if (me != null && (me.getRights() == PlayerRights.admin || me.getRights() == PlayerRights.mod))
        {
            return Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);
        }

        return false;
    }

    public boolean isFriendly()
    {
        return BrainOutClient.ClientController.getMyRemoteClient().getInfoBoolean("friendly", false);
    }

    public void toggleFriendly()
    {
        BrainOutClient.ClientController.sendTCP(new SetFriendlyStatusMsg(!isFriendly()));
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
    }

    private void onSummaryReceived(JSONObject summary, boolean alive)
    {
        this.summary = summary;

        if (!(BrainOutClient.getInstance().topState() instanceof ActionPhaseState ))
            return;

        ActionPhaseState ps = ((ActionPhaseState) BrainOutClient.getInstance().topState());
        if (alive)
        {
            ps.pushMenu(new FreePlaySummaryMenu(summary, true));
            return;
        }

        int myId = BrainOutClient.ClientController.getMyId();
        RemoteClient myRemoteClient = BrainOutClient.ClientController.getMyRemoteClient();

        String myPartyId = myRemoteClient != null ? myRemoteClient.getPartyId() : null;

        ActiveData follow = null;


        if (myPartyId != null)
        {
            for (ObjectMap.Entry<Integer, RemoteClient> entry : BrainOutClient.ClientController.getRemoteClients())
            {
                RemoteClient remoteClient = entry.value;

                if (remoteClient.getId() == myId)
                    continue;

                if (myPartyId.equals(remoteClient.getPartyId()))
                {
                    for (Map map : Map.All())
                    {
                        for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
                        {
                            if (activeData.getOwnerId() == remoteClient.getId())
                            {
                                follow = activeData;

                                break;
                            }
                        }

                        if (follow != null)
                            break;
                    }

                    break;
                }
            }
        }

        if (follow != null)
        {
            ActiveData finalFollow = follow;

            ps.addAction(new MenuAction()
            {
                @Override
                public void run()
                {
                    ps.popAllUntil(ActionPhaseMenu.class);
                    ps.pushMenu(new FreePlayFollowFriendMenu(finalFollow.getMap(), finalFollow, this::done));
                }
            });
        }
        else
        {
            ps.addAction(new WaitAction(2.0f));
        }

        ps.addAction(new MenuAction()
        {
            @Override
            public void run()
            {
                MenuAction menuAction = this;

                ps.getActionList().clearActions();

                ps.popAllUntil(ActionPhaseMenu.class);
                ps.pushMenu(new FadeInMenu(1.0f, () ->
                    ps.pushMenu(new FreePlaySummaryMenu(summary, false)),
                    menuAction::done));
            }
        });
    }

    private boolean realEstateMode(ActiveData activeData)
    {
        FreePlayMap map = activeData.getMap(FreePlayMap.class);
        if (map == null)
            return false;

        RealEstateInfo rs = map.getRealEstateInfo();
        if (rs == null)
            return false;

        return rs.owner != null && rs.owner.equals(BrainOutClient.ClientController.getMyAccount());
    }

    @Override
    public void currentPlayerDimensionChanged(ActiveData activeData)
    {
        ClientPlayerComponent cpc = activeData.getComponent(ClientPlayerComponent.class);
        if (cpc == null)
            return;

        boolean rsMode = realEstateMode(activeData);

        if (rsMode && (!(BrainOutClient.getInstance().topState().topMenu() instanceof RealEstateActionMenu)))
        {
            BrainOutClient.getInstance().topState().pushMenu(new RealEstateActionMenu());
        }
        else if (!rsMode && (BrainOutClient.getInstance().topState().topMenu() instanceof RealEstateActionMenu))
        {
            BrainOutClient.getInstance().topState().topMenu().pop();
        }
    }

    @Override
    public void openExchangeMenu(PlayerData myPlayerData)
    {
        if (myPlayerData != null && !myPlayerData.isWounded())
        {
            if (myPlayerData.getMap() instanceof FreePlayMap)
            {
                FreePlayMap fp = myPlayerData.getMap(FreePlayMap.class);
                RealEstateInfo rs = fp.getRealEstateInfo();
                if (rs != null && BrainOutClient.ClientController.getMyAccount().equals(rs.owner))
                {
                    ActiveProgressVisualComponentData progress = myPlayerData.getComponent(ActiveProgressVisualComponentData.class);

                    if (progress != null && progress.isActive())
                    {
                        return;
                    }

                    BrainOutClient.getInstance().topState().pushMenu(new RealEstateEditMenu(myPlayerData));
                    return;
                }
            }

            MyPlayerComponent mpc = myPlayerData.getComponent(MyPlayerComponent.class);

            if (mpc != null)
            {
                mpc.setMoveDirection(0, 0);
            }

            ActiveData marketContainer = MarketExchangeInventoryMenu.GetMarketContainer(myPlayerData);
            if (marketContainer != null)
            {
                ClientMarketContainerComponent cc = marketContainer.getCreator().getComponent(ClientMarketContainerComponent.class);
                if (!cc.getCategory().equals("default"))
                {
                    ClientMenuActivatorComponentData aaa = marketContainer.getComponent(ClientMenuActivatorComponentData.class);
                    if (aaa != null && aaa.getContentComponent().getMenu().equals("crafting"))
                    {
                        BrainOutClient.getInstance().topState().pushMenu(new MarketCraftingMenu(
                                BrainOutClient.ClientController.getState(CSGame.class).getPlayerData()));
                    }
                    else
                    {
                        BrainOutClient.getInstance().topState().pushMenu(new MarketItemsCategoryMenu(
                            BrainOutClient.ClientController.getState(CSGame.class).getPlayerData(),  cc.getCategory()));
                    }
                }
                else
                {
                    BrainOutClient.getInstance().topState().pushMenu(new MarketExchangeInventoryMenu(myPlayerData, marketContainer));
                }
            }
            else
            {
                BrainOutClient.getInstance().topState().pushMenu(new ExchangeInventoryMenu(myPlayerData));
            }
        }
    }
}
