package com.desertkun.brainout.client;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.common.ReflectionReceiver;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.data.NotifyData;
import com.desertkun.brainout.common.msg.*;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.shop.*;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.*;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.block.ConcreteBD;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.online.Preset;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateGame;
import com.desertkun.brainout.playstate.ServerPSGame;
import com.desertkun.brainout.server.ServerConstants;
import com.desertkun.brainout.server.ServerController;
import com.desertkun.brainout.utils.SharedValue;

import com.desertkun.brainout.common.msg.client.*;
import com.desertkun.brainout.common.msg.server.*;
import com.esotericsoftware.minlog.Log;
import org.json.JSONObject;

import java.util.*;

@SuppressWarnings("PointlessBooleanExpression")
public class Client extends ReflectionReceiver implements ActiveData.ComponentWriter, ReliableManager.MessageRecipient
{
    private final int id;
    private final ServerController serverController;
    private int spawnsCount;
    private boolean spectator;
    private Team team;
    private PlayerRights rights;

    private float timeSpent;
    private int killStreak;
    private float timeSinceLastKill;
    private int doubleKillsThisGame;
    private int friendlyKills;
    private int flagsCapturedThisGame;
    private int killsInAGame;
    private int safesOpenedThisGame;

    private Vector2 watchingPoint;

    private float spawnDelay;
    private float score;
    private int deaths;
    private int kills;
    private int killsWithoutDeath;
    private ActiveData.LastHitInfo lastHitInfo;
    private DisconnectReason disconnectReason;
    private SharedValue.Container<Integer> stuckContainer;
    private State state;

    protected Spawnable spawnAt;
    protected Set<Client> killedBy;
    protected ShopCart shopCart;
    protected ServerPlayerControllerComponentData playerController;
    protected PlayerData playerData, recentPlayerData;
    protected int lastPlayerId;
    private ModePayload modePayload;

    public enum State
    {
        none,
        mapInitialized,
        readyToSpawn,
        spawnDelay,
        spawned,
        reconnect,
        roconnectTimeout
    }

    public void reset()
    {
        spawnsCount = 0;
        spectator = false;
        team = null;
        shopCart.clear();
        playerData = null;
        lastPlayerId = -1;
        killedBy.clear();
        killStreak = 0;
        flagsCapturedThisGame = 0;
        doubleKillsThisGame = 0;
        killsInAGame = 0;
        timeSinceLastKill = 0;
        friendlyKills = 0;
        spawnDelay = 0;
        score = 0;
        kills = 0;
        deaths = 0;

        if (state != State.reconnect)
        {
            state = State.none;
        }

        killsWithoutDeath = 0;
        spawnAt = null;
        timeSpent = 0;
        stuckContainer.clear();
        watchingPoint.set(0, 0);

        getServerController().cancelSpawn(this);
    }

    public Client(int id, ServerController serverController)
    {
        this.id = id;
        this.serverController = serverController;
        this.shopCart = new ShopCart();
        this.team = null;
        this.disconnectReason = DisconnectReason.connectionError;
        this.friendlyKills = 0;
        this.state = State.none;

        this.watchingPoint = new Vector2();
        this.rights = PlayerRights.none;
        this.timeSinceLastKill = 0;
        this.killStreak = 0;
        this.spectator = false;
        this.killsWithoutDeath = 0;
        this.spawnsCount = 0;
        this.killedBy = new HashSet<>();
        this.stuckContainer = new SharedValue.MapContainer<>();
        this.modePayload = null;

        if (!BrainOut.OnlineEnabled())
        {
            this.rights = PlayerRights.admin;
        }
    }

    @SuppressWarnings("unused")
    public boolean received(WatchPointMsg msg)
    {
        watchingPoint.set(msg.x, msg.y);
        return true;
    }

    public boolean catTrackStatsWithBots()
    {
        // allow to farm bots up until 10 level
        return getLevel(Constants.User.LEVEL, 0) < 10;
    }

    public void onDeath(Client killer, PlayerData playerData, InstrumentInfo info)
    {
        killsWithoutDeath = 0;

        GameMode gameMode = getServerController().getGameMode();
        boolean trackStats = true;

        if (!(killer instanceof PlayerClient))
        {
            trackStats = catTrackStatsWithBots();
        }

        if (gameMode != null && gameMode.countDeaths())
        {
            setDeaths(getDeaths() + 1);

            if (trackStats)
            {
                addStat(Constants.Stats.DEATHS, 1);
                updateEfficiency();
            }
        }

        PlayState playState = getServerController().getPlayState();

        if (playState instanceof ServerPSGame)
        {
            ServerPSGame psGame = ((ServerPSGame) playState);

            psGame.doCheck();

            if (!psGame.isGameFinished())
            {
                ServerRealization serverRealization = ((ServerRealization) psGame.getMode().getRealization());
                serverRealization.onClientDeath(this, killer, playerData, info);
            }
        }

        BrainOutServer.Controller.onDeath(this);
    }

    public void updateEfficiency()
    {
        float kills = getStat(Constants.Stats.KILLS, 1.0f);
        float deaths = getStat(Constants.Stats.DEATHS, 1.0f);

        if (deaths > 100 && kills > 1500)
        {
            setStat(Constants.Stats.EFFICIENCY, kills / deaths);
        }
        else
        {
            setStat(Constants.Stats.EFFICIENCY, 0);
        }
    }

    protected Player getSpawnPlayer()
    {
        Player player = shopCart.getPlayer();

        if (player == null)
        {
            PlayerSlot playerSlot = BrainOutServer.ContentMgr.get(Constants.User.PLAYER_SLOT, PlayerSlot.class);

            if (playerSlot == null)
            {
                return null;
            }

            player = ((PlayerSlotItem) playerSlot.getDefaultItem()).getPlayer();
        }

        return player;
    }

    public boolean spawn(Spawnable spawnAt)
    {
        if (isSpectator())
        {
            return false;
        }
        else
        {
            PlayState playState = getServerController().getPlayState();

            if (BrainOut.R.instanceOf(PlayStateGame.class, playState))
            {
                PlayStateGame game = ((PlayStateGame) playState);

                switch (((ServerRealization) game.getMode().getRealization()).acquireSpawn(this, team))
                {
                    case forceSpectator:
                    {
                        requestSpectate();
                        return false;
                    }
                    case disallowed:
                    {
                        return false;
                    }
                }
            }
        }

        setState(State.spawned);

        spawnsCount++;

        PlayState playState = getServerController().getPlayState();

        shopCart.checkRestrictions();

        Player player = getSpawnPlayer();
        if (player == null)
            return false;

        Map map = Map.Get(spawnAt.getDimension());

        if (map == null)
            return false;

        playerData = (PlayerData)player.getData(spawnAt.getDimension());

        getServerController().getClients().registerActive(playerData, this);
        playerData.setTeam(team);

        playerData.setOwnerId(getId());

        ServerPlayerControllerComponentData controller = newPlayerController();
        PlayerOwnerComponent ownerComponent = new PlayerOwnerComponent(playerData);
        PlayerRemoteComponent remoteComponent = new PlayerRemoteComponent(playerData);

        playerData.addComponent(controller);
        playerData.addComponent(ownerComponent);
        playerData.addComponent(remoteComponent);

        playerController = playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);
        
        if (playerController == null)
        {
            disconnect(DisconnectReason.badPlayer, "playerController == null");
        }
        else
        {
            playerController.setClient(this);
        }

        float spawnX = spawnAt.getSpawnX();
        float spawnY = spawnAt.getSpawnY();
        SpawnTarget target = spawnAt.getTarget();

        if (spawnAt.getSpawnRange() != 0)
        {
            Array<SubPointComponentData> subpoints = new Array<>();
            Array<PlayerData> enemies = new Array<>();
            final ObjectMap<SubPointComponentData, Integer> seen = new ObjectMap<>();

            for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.SPAWNABLE, false))
            {
                if (activeData.getComponent(SubPointComponentData.class) != null &&
                        Vector2.dst(activeData.getX(), activeData.getY(),
                                spawnX, spawnY) <= spawnAt.getSpawnRange())
                {
                    SubPointComponentData subPointData = activeData.getComponent(SubPointComponentData.class);

                    if (subPointData.getTarget() == target &&
                            (activeData.getTeam() == null || activeData.getTeam() == team))
                    {
                        if (subPointData.isAvailable())
                        {
                            subpoints.add(subPointData);
                        }
                    }
                }
            }

            for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
            {
                if (activeData instanceof PlayerData && isEnemy(activeData.getTeam()) &&
                        Vector2.dst(activeData.getX(), activeData.getY(),
                                spawnX, spawnY) <= spawnAt.getSpawnRange())
                {
                    enemies.add(((PlayerData) activeData));
                }
            }

            if (subpoints.size > 0)
            {
                for (SubPointComponentData subpoint : subpoints)
                {
                    int s = 0;

                    for (PlayerData enemy : enemies)
                    {
                        ActiveData activeData = ((ActiveData) subpoint.getComponentObject());

                        if (activeData.isVisible(enemy))
                        {
                            s++;
                        }
                    }

                    seen.put(subpoint, s);
                }

                subpoints.shuffle();

                subpoints.sort((o1, o2) ->
                {
                    int s1 = seen.get(o1);
                    int s2 = seen.get(o2);

                    if (s1 > s2)
                    {
                        return 1;
                    }
                    else if (s1 < s2)
                    {
                        return -1;
                    }
                    else
                    {
                        return Math.random() > 0.5 ? 1 : -1;
                    }
                });

                SubPointComponentData subpoint = subpoints.get(0);

                subpoint.take();

                ActiveData activeData = ((ActiveData) subpoint.getComponentObject());

                spawnX = activeData.getX() + subpoint.getOffset().x;
                spawnY = activeData.getY() + subpoint.getOffset().y;
            }
        }

        if (spawnAt instanceof FlagData)
        {
            HealthComponentData hcd = playerData.getComponent(HealthComponentData.class);
            if (hcd != null)
            {
                hcd.setImmortalTime(0);
            }
        }

        playerData.setPosition(spawnX, spawnY);

        updatePlayerSelection(shopCart);

        Shop shop = Shop.getInstance();
        Preset preset = BrainOutServer.Controller.getCurrentPreset();

        for (ObjectMap.Entry<Slot, SlotItem.Selection> slotItem : shopCart.getItems())
        {
            if (!slotItem.value.getItem().isEnabled())
                continue;

            if (!shop.getSlots().contains(slotItem.key, true))
                continue;

            if (preset != null)
            {
                if (!preset.isSlotAllowed(slotItem.key))
                    continue;

                if (!preset.isItemAllowed(slotItem.value.getItem()))
                    continue;
            }

            applySelection(shopCart, playerData, slotItem.key, slotItem.value);
        }

        if (BrainOut.R.instanceOf(PlayStateGame.class, playState))
        {
            PlayStateGame game = ((PlayStateGame) playState);

            ((ServerRealization) game.getMode().getRealization()).onClientSpawn(this, playerData);
        }

        remoteComponent.setCurrentInstrument(ownerComponent.getCurrentInstrument());
        remoteComponent.setHookedInstrument(ownerComponent.getHookedInstrument());

        beforeInit(playerData);
        playerData.init();

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode != null && ((ServerRealization) gameMode.getRealization()).displaceBlocksUponSpawn())
        {
            SimplePhysicsComponentData phy = playerData.getComponentWithSubclass(SimplePhysicsComponentData.class);

            if (phy != null)
            {
                Vector2 halfSize = phy.getHalfSize();

                float aX = spawnX - halfSize.x, bX = spawnX + halfSize.x,
                        aY = spawnY - halfSize.y, bY = spawnY + halfSize.y;

                for (float x = aX; x <= bX; x += 1)
                {
                    for (float y = aY; y <= bY; y += 1)
                    {
                        BlockData block = map.getBlockAt(x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

                        if (block instanceof ConcreteBD)
                        {
                            BrainOut.EventMgr.sendDelayedEvent(block, DestroyEvent.obtain());
                        }
                    }
                }
            }
        }

        map.addActive(map.generateServerId(), playerData, false, true, Client.this);

        lastPlayerId = playerData.getId();

        return true;
    }

    protected void beforeInit(PlayerData playerData)
    {

    }

    protected void updatePlayerSelection(ShopCart shopCart)
    {

    }

    protected ServerPlayerControllerComponentData newPlayerController()
    {
        return new ServerPlayerControllerComponentData(playerData);
    }

    public void disconnect(DisconnectReason reason, String message) {}

    protected void applySelection(ShopCart shopCart, PlayerData playerData, Slot key, SlotItem.Selection value)
    {

    }

    public ConsumableRecord addConsumable(int amount, ConsumableItem consumableItem, int quality)
    {
        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
        ConsumableContainer consumableContainer = poc.getConsumableContainer();

        ConsumableRecord record = consumableContainer.putConsumable(amount, consumableItem, quality);
        sendConsumable();
        return record;
    }

    public void sendConsumable() {}

    public void notify(NotifyAward notifyAward, float amount, NotifyReason reason,
                       NotifyMethod method, NotifyData data, boolean priority)
    {
    }

    public void notify(NotifyAward notifyAward, float amount, NotifyReason reason,
                       NotifyMethod method, NotifyData data)
    {
        notify(notifyAward, amount, reason, method, data, false);
    }

    public void award(NotifyAward notifyAward, float amount)
    {
        switch (notifyAward)
        {
            case score:
            {
                addScore(amount, true);
                break;
            }
            case techScore:
            {
                addStat(Constants.User.TECH_SCORE, amount);
                break;
            }
        }
    }

    @Override
    public boolean canSend(int owner, Data data, Component component)
    {
        if (component instanceof HealthComponentData)
        {
            return true;
        }

        if (component instanceof PlayerOwnerComponent)
        {
            return ((PlayerData)data).getOwnerId() == owner;
        }

        return !(component instanceof PlayerRemoteComponent) || ((PlayerData)data).getOwnerId() != owner;
    }

    protected void requestSpectate()
    {
        setSpectator(true);
    }

    public void requestSpawn()
    {
        if (isAlive()) return;

        PlayState playState = getServerController().getPlayState();

        if (!BrainOut.R.instanceOf(PlayStateGame.class, playState)) return;

        PlayStateGame game = ((PlayStateGame) playState);

        if (game == null) return;
        if (game.getMode() == null) return;

        ServerRealization realization = ((ServerRealization) game.getMode().getRealization());

        switch (realization.canSpawn(getTeam()))
        {
            case disallowed:
            {
                return;
            }
            case forceSpectator:
            {
                requestSpectate();
            }
        }

        if (!game.isGameFinished())
        {
            setState(State.readyToSpawn);

            readyToSpawn();
        }
    }

    protected void readyToSpawn()
    {
    }

    public int getId()
    {
        return id;
    }

    public void sendTCPExcept(Object object)
    {
        getServerController().getClients().sendTCPExcept(object, this);
    }

    public void sendUDPExcept(UdpMessage object)
    {
        getServerController().getClients().sendUDPExcept(object, this);
    }

    public void kill()
    {
        kill(true, true);
    }

    public void kill(boolean countDeath, boolean ragdoll)
    {
        if (playerData != null)
        {
            BrainOut.EventMgr.sendDelayedEvent(playerData, DestroyEvent.obtain(ragdoll));
        }

        clearPlayerData();
    }

    protected void sendServerChat(String header, String message, Color color)
    {
        sendTCPExcept(new ChatMsg(header, message, "server", color, -1));
    }

    public void release()
    {
        if (isTeamSelected())
        {
            PlayState playState = BrainOutServer.Controller.getPlayState();
            if (BrainOut.R.instanceOf(PlayStateGame.class, playState))
            {
                PlayStateGame game = ((PlayStateGame) playState);
                if (game.getMode() != null)
                {
                    ServerRealization serverRealization = (ServerRealization) game.getMode().getRealization();
                    serverRealization.clientReleased(this);
                }
            }
        }

        kill();

        if (BrainOutServer.PackageMgr.getDefine("chat", "enabled").equals("enabled"))
        {
            if (!(getTeam() instanceof SpectatorTeam) && !BrainOutServer.Controller.isFreePlay())
            {
                sendServerChat("{MP_SERVER}", "{MP_PLAYER_DISCONNECTED," + getName() + "}",
                        ServerConstants.Chat.COLOR_INFO);
            }

            if (disconnectReason != DisconnectReason.leave &&
                    disconnectReason != DisconnectReason.connectionError)
            {
                sendTCPExcept(
                    new ChatMsg("{MP_SERVER}", "{MP_DISCONNECT_REASON," + disconnectReason.toString() + "}",
                        "server", ServerConstants.Chat.COLOR_INFO, -1)
                );
            }
        }

        sendTCPExcept(new RemoveRemoteClientMsg(getId()));
    }

    public ServerController getServerController()
    {
        return serverController;
    }

    public boolean isConnected()
    {
        return true;
    }

    public String getName()
    {
        return "unknown";
    }

    public Team getTeam()
    {
        return team;
    }

    public boolean isInitialized()
    {
        switch (getState())
        {
            case none:
                return false;

            case mapInitialized:
            case readyToSpawn:
            case spawnDelay:
            case spawned:
            case reconnect:
            case roconnectTimeout:
                return true;

            default:
                return true;
        }
    }

    public boolean isReconnecting()
    {
        return getState() == State.reconnect;
    }

    public boolean isTeamSelected()
    {
        return team != null && isInitialized();
    }

    public void update(float dt)
    {
        timeSpent += dt;

        if (timeSpent > 60)
        {
            timeSpent = 0;

            addStat(Constants.Stats.TIME_SPENT, 1);
        }

        switch (state)
        {
            case spawnDelay:
            {
                spawnDelay -= dt;
                if (spawnDelay <= 0)
                {
                    requestSpawn();
                }
                break;
            }
            case spawned:
            {
                if (playerData != null)
                {
                    Map map = playerData.getMap();

                    if (map != null)
                    {
                        float x = playerData.getX(), y = playerData.getY();

                        if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight())
                        {
                            kill();
                            setState(State.readyToSpawn);
                        }
                    }
                }

                break;
            }
        }

        if (timeSinceLastKill > 0)
        {
            timeSinceLastKill -= dt;

            if (timeSinceLastKill <= 0)
            {
                killStreak = 0;
                timeSinceLastKill = 0;
            }
        }

        if (getModePayload() != null)
        {
            getModePayload().update(dt);
        }
    }

    public long getPing()
    {
        return 0;
    }

    public void clearPlayerData()
    {
        if (playerData == null)
            return;

        getServerController().getClients().unregiterActive(playerData);

        recentPlayerData = playerData;
        playerData = null;

        synchronized (this)
        {
            BrainOutServer.Timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    recentPlayerData = null;
                }
            }, 150);
        }
    }

    public PlayerRights getRights()
    {
        return rights;
    }

    public void setRights(PlayerRights rights)
    {
        this.rights = rights;
    }

    public PlayerData getPlayerData()
    {
        return playerData;
    }

    public int getLastPlayerId()
    {
        return lastPlayerId;
    }

    public float getScore()
    {
        return score;
    }

    public float getEfficiency()
    {
        return 1;
    }

    public void addScore(float score, boolean addStat)
    {
        this.score += score;
    }

    public int getDeaths()
    {
        return deaths;
    }

    public void setDeaths(int deaths)
    {
        this.deaths = deaths;
    }

    public void setKills(int kills)
    {
        this.kills = kills;
    }

    public int getKills()
    {
        return kills;
    }

    public void setTeam(Team team)
    {
        this.team = team;
    }

    public boolean isAlive()
    {
        return playerData != null;
    }

    public PlayerData hasBeenAliveRecently()
    {
        if (playerData != null)
        {
            return playerData;
        }

        return recentPlayerData;
    }

    public void registerKill(boolean trackStats)
    {
        if (timeSinceLastKill > 0)
        {
            switch (killStreak)
            {
                case 1:
                {
                    if (trackStats)
                        addStat("double-kills", 1);

                    doubleKillsThisGame++;

                    if (doubleKillsThisGame == 4)
                    {
                        addStat("double-kills-4-game", 1);
                    }

                    if (doubleKillsThisGame == 3)
                    {
                        addStat("double-kills-3-game", 1);
                    }

                    if (doubleKillsThisGame == 2)
                    {
                        addStat("double-kills-2-game", 1);
                    }

                    float a = BrainOutServer.getInstance().getSettings().getPrice("doubleKill");
                    addScore(a, trackStats);
                    notify(NotifyAward.score, a, NotifyReason.doubleKill, NotifyMethod.message, null);

                    break;
                }
                case 2:
                {
                    if (trackStats)
                        addStat("triple-kills", 1);
                    float a = BrainOutServer.getInstance().getSettings().getPrice("tripleKill");
                    addScore(a, trackStats);
                    notify(NotifyAward.score, a, NotifyReason.tripleKill, NotifyMethod.message, null);

                    break;
                }
            }
        }

        timeSinceLastKill = 2f;
        killStreak += 1;
        killsWithoutDeath += 1;

        if (killsWithoutDeath == 3)
        {
            addStat("kills-3-no-death", 1);
        }
    }

    public void setLastHitInfo(ActiveData.LastHitInfo lastHitInfo)
    {
        this.lastHitInfo = lastHitInfo;
    }

    public ActiveData.LastHitInfo getLastHitInfo()
    {
        return lastHitInfo;
    }

    public void nextRespawnIn(float time)
    {
    }

    public boolean isValidSpawn()
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();
        return gameMode != null && gameMode.canSpawn(spawnAt, getTeam());
    }

    public void doSpawn()
    {
        if (isAlive()) return;

        if (spawnAt == null)
            return;

        if (!spawn(spawnAt))
        {
            cantSpawn();
        }
    }

    protected void cantSpawn()
    {

    }

    public Spawnable getSpawnAt()
    {
        return spawnAt;
    }

    public void setSpawnAt(Spawnable spawnAt)
    {
        this.spawnAt = spawnAt;
    }

    public int getLevel(String kind, int def)
    {
        return def;
    }

    public boolean isSpectator()
    {
        return spectator || (team instanceof SpectatorTeam);
    }

    public boolean setSpectator(boolean spectator)
    {
        if (this.spectator == spectator)
            return false;

        this.spectator = spectator;

        return true;
    }

    public float addStat(String stat, float amount) { return 0; }
    public float getStat(String stat, float def) { return 0; }
    public float setStat(String stat, float value) { return 0; }

    public String getPartyId()
    {
        return "";
    }

    public boolean isAllowDrop()
    {
        return true;
    }

    public float addClanStat(String stat, float amount) { return 0; }

    public int getKillsWithoutDeath()
    {
        return killsWithoutDeath;
    }

    public void kick(String message)
    {
        disconnect(DisconnectReason.kicked, message);
    }

    public void setDisconnectReason(DisconnectReason disconnectReason)
    {
        this.disconnectReason = disconnectReason;
    }

    public void showPopup(String title, String data)
    {
    }

    public void setName(String name)
    {
    }

    public int getSpawnsCount()
    {
        return spawnsCount;
    }

    public void decFriendlyKills()
    {
        friendlyKills--;
    }

    public int incFriendlyKills()
    {
        friendlyKills++;
        return friendlyKills;
    }

    public void addKilledBy(Client client)
    {
        killedBy.add(client);
    }

    public int getKillStreak()
    {
        return killStreak;
    }

    public int getFlagsCapturedThisGame()
    {
        return flagsCapturedThisGame;
    }

    public void setFlagsCapturedThisGame(int flagsCapturedThisGame)
    {
        this.flagsCapturedThisGame = flagsCapturedThisGame;
    }

    public int getKillsInAGame()
    {
        return killsInAGame;
    }

    public void setKillsInAGame(int killsInAGame)
    {
        this.killsInAGame = killsInAGame;
    }

    public void setSafesOpenedThisGame(int safesOpenedThisGame)
    {
        this.safesOpenedThisGame = safesOpenedThisGame;
    }

    public int getSafesOpenedThisGame()
    {
        return safesOpenedThisGame;
    }

    public boolean isEnemy(Team team)
    {
        PlayState ps = BrainOutServer.Controller.getPlayState();
        if (ps instanceof PlayStateGame)
        {
            PlayStateGame playStateGame = ((PlayStateGame) ps);
            GameMode gameMode = playStateGame.getMode();
            return gameMode.isEnemies(getTeam(), team);
        }

        return getTeam() != team;
    }

    public JSONObject getInfo()
    {
        JSONObject info = new JSONObject();

        if (getModePayload() != null)
        {
            getModePayload().getInfo(info);
        }

        return info;
    }

    public boolean isEnemy(Client team)
    {
        if (team == this)
        {
            return false;
        }

        PlayState ps = BrainOutServer.Controller.getPlayState();
        if (ps instanceof PlayStateGame)
        {
            PlayStateGame playStateGame = ((PlayStateGame) ps);
            GameMode gameMode = playStateGame.getMode();

            return gameMode.isEnemies(getTeam(), team.getTeam()) ||
                ((ServerRealization) gameMode.getRealization()).isEnemies(this, team);
        }

        return getTeam() != team.getTeam();
    }

    public SharedValue.Container<Integer> getStuckContainer()
    {
        return stuckContainer;
    }

    public Vector2 getWatchingPoint()
    {
        return watchingPoint;
    }

    public PlayerControllerComponentData getPlayerController()
    {
        return playerController;
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
        if (this.state == State.reconnect)
        {
            switch (state)
            {
                case readyToSpawn:
                case spawnDelay:
                case spawned:
                {
                    return;
                }
            }
        }

        this.state = state;
    }

    public void requestSpawnWithDelay()
    {
        setState(State.spawnDelay);

        spawnDelay = generateSpawnDelay();
    }

    private float generateSpawnDelay()
    {
        PlayState playState = BrainOutServer.Controller.getPlayState();
        if (BrainOut.R.instanceOf(PlayStateGame.class, playState))
        {
            PlayStateGame game = ((PlayStateGame) playState);
            if (game.getMode() != null)
            {
                ServerRealization serverRealization = (ServerRealization) game.getMode().getRealization();
                return serverRealization.getSpawnDelay();
            }
        }

        return ServerConstants.Spawn.DELAY;
    }

    protected void forgivedBy(Client client) {}

    public void teamChanged(Team team) {}

    public void sendRemotePlayers()
    {
        sendRemotePlayers(true);
    }

    public void sendRemotePlayers(boolean except)
    {
        if (getTeam() == null)
            return;

        NewRemoteClientMsg msg = new NewRemoteClientMsg(
                getId(), getName(), getAvatar(), getClanAvatar(), getClanId(),
                getTeam(), getRights(), getInfo());

        if (except)
        {
            sendTCPExcept(msg);
        }
        else
        {
            BrainOutServer.Controller.getClients().sendTCP(msg);
        }
    }

    public DisconnectReason getDisconnectReason()
    {
        return disconnectReason;
    }

    public void store()
    {
        //
    }

    public boolean isCheater()
    {
        return false;
    }

    public boolean isSocialFriendOf(Client client)
    {
        return false;
    }

    protected String getLogHeader()
    {
        String header = "[" + getId() + "] ";

        if (!getName().equals("unknown"))
        {
            header += "( \"" + getName() + "\" ) ";
        }

        return header;
    }

    public void log(String data)
    {
        if (Log.INFO)
        {
            Log.info(getLogHeader() + data);
        }
    }

    protected void logError(String data)
    {
        if (Log.ERROR)
        {
            Log.error(getLogHeader() + data);
        }
    }

    public boolean onDisconnect()
    {
        log("Disconnected: " + disconnectReason.toString());

        return true;
    }

    public String getAvatar()
    {
        return "";
    }

    public String getClanAvatar()
    {
        return "";
    }

    public String getClanId()
    {
        return "";
    }

    public boolean isParticipatingClan()
    {
        return false;
    }

    public ShopCart getShopCart()
    {
        return shopCart;
    }

    public ModePayload getModePayload()
    {
        return modePayload;
    }

    public void setModePayload(ModePayload modePayload)
    {
        this.modePayload = modePayload;
    }

    public void completelyInitialized()
    {
        GameMode gameMode = getServerController().getGameMode();

        if (gameMode != null)
        {
            ModePayload payload = getModePayload();

            if (payload != null)
            {
                payload.init();
            }
        }
    }

    public void enablePlayer(boolean enable)
    {
        if (playerData == null)
            return;

        PlayerOwnerComponent own =
                playerData.getComponent(PlayerOwnerComponent.class);

        if (own == null)
            return;

        ServerPlayerControllerComponentData ctr =
            playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

        if (ctr == null)
            return;

        own.setEnabled(enable);
        ctr.setEnabled(enable);
    }

    public void forceSpawn()
    {
        if (spawnAt == null || this.team.getID() != spawnAt.getTeam().getID())
        {
            for (Map map : Map.All())
            {
                for (ActiveData data : map.getActivesForTag(Constants.ActiveTags.SPAWNABLE, false))
                {
                    if (data instanceof SpawnPointData)
                    {
                        if (((SpawnPointData) data).canSpawn(getTeam()))
                        {
                            spawnAt = ((SpawnPointData) data);
                            doSpawn();
                            return;
                        }
                    }
                }
            }
        }

        doSpawn();
    }
}
