package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskHunter;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.ReflectionReceiver;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.msg.ModeMessage;
import com.desertkun.brainout.common.msg.server.ModeUpdatedMsg;
import com.desertkun.brainout.common.msg.server.PlayMusicMsg;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FlagTakenEvent;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.online.RoomSettings;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateEndGame;
import com.desertkun.brainout.playstate.ServerPSGame;
import com.desertkun.brainout.server.ServerConstants;
import com.esotericsoftware.minlog.Log;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class ServerRealization<G extends GameMode> extends GameModeRealization<G>
{
    private ReflectionReceiver receiver;
    private PlayerClient messageClient;
    private ObjectMap<FlagData, Team> takenFlags;
    private int clientsCount;
    private int maxPlayers;
    private float checkTimer;

    public enum SpawnMode
    {
        disallowed,
        forceSpectator,
        allowed
    }

    public ServerRealization(G gameMode)
    {
        super(gameMode);

        this.takenFlags = new ObjectMap<>();
        this.receiver = new ReflectionReceiver();
        this.clientsCount = 0;
        this.maxPlayers = 0;
    }

    public void skipWarmUp()
    {
        if (getGameMode().getPhase() != GameMode.Phase.warmUp)
            return;

        if (getGameMode().getTimer() > 0)
        {
            getGameMode().forceTimer();
        }
    }

    public void warmUp()
    {
        if (Log.INFO) Log.info("Warm up!");

        if (getGameMode().hasWarmUp())
        {
            getGameMode().setTimer(45, this::warmUpComplete);
            startWarmup();
            BrainOutServer.PostRunnable(this::updated);
        }
    }

    private void startWarmup()
    {
        getGameMode().setPhase(GameMode.Phase.warmUp);

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);
                playerClient.setTookPartInWarmup(true);
            }
        }

        BrainOutServer.Controller.updateRoomSettings();
    }

    public ModePayload newPlayerPayload(Client playerClient)
    {
        return null;
    }

    public boolean needWayPoints()
    {
        return true;
    }

    protected void warmUpComplete()
    {
        if (Log.INFO) Log.info("Warm up complete!");

        setRoundTime();

        if (BrainOutServer.Controller.getClients().getAutobalance().isEnabled())
        {
            BrainOutServer.Controller.getClients().sortBalance();
        }
        else
        {
            for (Client client : BrainOutServer.Controller.getClients().values())
            {
                if (client.isInitialized())
                {
                    client.kill(false, false);
                }
            }
        }

        for (Client client : BrainOutServer.Controller.getClients().values())
        {
            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);

                playerClient.notify(NotifyAward.none, 0, NotifyReason.warmupComplete,
                        NotifyMethod.message, null);
            }
        }

        getGameMode().setPhase(GameMode.Phase.game);

        // rebuild map once complete

        for (ServerMap map : Map.All(ServerMap.class))
        {
            if (map.needWayPoints())
            {
                map.getWayPointMap().regenerate();
            }
        }

        BrainOutServer.PostRunnable(this::updated);

        BrainOutServer.Controller.updateRoomSettings();
    }

    protected void setRoundTime()
    {
        long roundTime = BrainOutServer.Settings.getRoundTime();

        PlayState playState = BrainOutServer.Controller.getPlayState();

        if (playState instanceof ServerPSGame)
        {
            ServerPSGame psGame = ((ServerPSGame) playState);

            if (psGame.getCurrentMode().getRoundTime() != -1)
            {
                roundTime = psGame.getCurrentMode().getRoundTime();
            }
        }

        if (roundTime != 0)
        {
            getGameMode().setEndTime(roundTime);
        }
    }

    public abstract boolean isComplete(PlayStateEndGame.GameResult gameResult);
    public abstract boolean timedOut(PlayStateEndGame.GameResult gameResult);

    protected boolean timedOutDraw(PlayStateEndGame.GameResult gameResult)
    {
        OrderedMap<Team, Integer> scores = new OrderedMap<>();

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (client.getTeam() == null || client.getTeam() instanceof SpectatorTeam)
                continue;

            scores.put(client.getTeam(), scores.get(client.getTeam(), 0) + (int)client.getScore());
        }

        if (scores.size < 1)
            return false;

        scores.orderedKeys().sort((o1, o2) -> scores.get(o2, 0) - scores.get(o1, 0));

        gameResult.setTeamWon(scores.orderedKeys().get(0));
        return true;
    }

    public boolean received(Object from, ModeMessage modeMessage)
    {
        this.messageClient = ((PlayerClient) from);

        return receiver.received(modeMessage, this);
    }

    public void updated(Client client)
    {
        if (client instanceof PlayerClient)
        {
            ((PlayerClient) client).sendTCP(
                new ModeUpdatedMsg(getGameMode(), client.getId()));
        }
    }

    public void updated()
    {
        for (ObjectMap.Entry<Integer, Client> entry : new ObjectMap.Entries<>(BrainOutServer.Controller.getClients()))
        {
            Client client = entry.value;
            if (client instanceof PlayerClient)
            {
                ((PlayerClient) client).sendTCP(
                        new ModeUpdatedMsg(getGameMode(), client.getId()));
            }
        }
    }

    @Override
    public void update(float dt)
    {
        checkTimer -= dt;

        if (checkTimer < 0)
        {
            checkTimer = 0.5f;

            switch (getGameMode().getPhase())
            {
                case game:
                {
                    if (getGameMode().isAboutToEnd())
                    {
                        getGameMode().setPhase(GameMode.Phase.aboutToEnd);

                        BrainOutServer.Controller.updateRoomSettings();
                    }

                    break;
                }
            }
        }
    }

    public void check() {}

    public void onClientDamaged(Client client, PlayerData playerData, String kind) {}

    private void flagTaken(Team team, FlagData flagData)
    {
        if (team != null)
        {
            takenFlags.put(flagData, team);
        }
        else
        {
            takenFlags.remove(flagData);
        }

        updated();
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case flagTaken:
            {
                FlagTakenEvent flagTakenEvent = ((FlagTakenEvent) event);

                flagTaken(flagTakenEvent.team, flagTakenEvent.flagData);

                break;
            }
        }

        return false;
    }

    public void onClientReconnecting(PlayerClient playerClient, PlayerData playerData)
    {
    }

    public boolean immediateRespawn()
    {
        return false;
    }

    public PlayerClient getMessageClient()
    {
        return messageClient;
    }

    public void setMessageClient(PlayerClient messageClient)
    {
        this.messageClient = messageClient;
    }

    public ObjectMap<FlagData, Team> getTakenFlags()
    {
        return takenFlags;
    }

    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.disallowed;
    }

    public SpawnMode canSpawn(Team team)
    {
        return SpawnMode.allowed;
    }

    public void onClientSpawn(Client client, PlayerData player)
    {
        
    }

    public void onClientReconnect(PlayerClient client, PlayerData playerData)
    {

    }

    public void onClientDeath(Client client, Client killer, PlayerData playerData, InstrumentInfo info)
    {
        client.requestSpawnWithDelay();
    }

    public void finished()
    {

    }

    public boolean isEnemies(Client a, Client b)
    {
        return false;
    }

    @Override
    public boolean isEnemies(int ownerA, int ownerB)
    {
        Client a = BrainOutServer.Controller.getClients().get(ownerA);
        Client b = BrainOutServer.Controller.getClients().get(ownerB);

        if (a == null || b == null)
            return false;

        return isEnemies(a, b) || getGameMode().isEnemies(a.getTeam(), b.getTeam());
    }

    protected void maxPlayersIncreased(int diff) {}

    public void voiceChat(PlayerClient playerClient, short[] data, float volume) {}

    @Override
    public void init(PlayState.InitCallback callback)
    {
        super.init(success ->
        {
            if (!getGameMode().hasWarmUp())
            {
                setupPhase();
            }

            callback.done(success);
        });

        initSettings(BrainOutServer.Controller.getInitRoomSettings());
    }

    protected void setupPhase()
    {
        setRoundTime();
        getGameMode().setPhase(GameMode.Phase.game);
    }

    public void newPlayerClient(PlayerClient playerClient) {}

    public void onSelectionUpdated(PlayerClient playerClient) {}

    public void clientInitialized(Client client, boolean reconnected)
    {
        clientsCount++;

        if (!reconnected)
        {
            if (clientsCount > maxPlayers)
            {
                maxPlayersIncreased(clientsCount - maxPlayers);

                maxPlayers = clientsCount;
            }
        }
    }

    public void clientCompletelyInitialized(Client client)
    {

    }

    public boolean forceWeaponAutoLoad()
    {
        return false;
    }

    public void clientReleased(Client client)
    {
        clientsCount--;

        if (clientsCount < 0)
        {
            clientsCount = 0;
        }
    }

    public void initSettings(RoomSettings settings)
    {
        // do nothing
    }

    protected void playMusic(String music)
    {
        BrainOutServer.Controller.getClients().sendTCP(new PlayMusicMsg(music));
    }

    public boolean enableLoginPopup()
    {
        return true;
    }

    public boolean hasFinishedTimer()
    {
        return true;
    }

    public boolean needsDeploymentsCheck()
    {
        return true;
    }

    public boolean canDropConsumable(Client playerClient, ConsumableItem item)
    {
        return true;
    }

    public Set<String> getSuitableDimensions(Client client)
    {
        Set<String> d = new LinkedHashSet<>();

        for (Map map : Map.SafeAll())
        {
            d.add(map.getDimension());
        }

        return d;
    }

    public float getSpawnDelay()
    {
        return ServerConstants.Spawn.DELAY;
    }

    public boolean canTakeFlags()
    {
        return false;
    }

    public boolean displaceBlocksUponSpawn()
    {
        return !BrainOutServer.IsCustom();
    }

    public boolean isDeathDropEnabled(PlayerData playerData)
    {
        return true;
    }

    public boolean isFullDropEnabled(PlayerData playerData)
    {
        return true;
    }

    public void onUnknownPlayerDeath(PlayerData playerData, Client killer) {}

    public boolean awardScores()
    {
        return true;
    }

    public float calculateDamage(Team receiverTeam, Team senderTeam, int receiverId, int senderId, float dmg)
    {
        return dmg;
    }

    public boolean spectatorsCanSeeEnemies()
    {
        return BrainOutServer.PackageMgr.getDefine("spectatorsCanSeeEnemies", "true").equals("true");
    }

    public Spawnable chooseBotSpawnPoint(Array<Spawnable> spawnables)
    {
        spawnables.shuffle();

        for (Spawnable spawnable : spawnables)
        {
            // flags first
            if (spawnable instanceof FlagData)
                return spawnable;
        }

        return spawnables.random();
    }

    public Task getBotStartingTask(TaskStack taskStack, BotClient client)
    {
        return null;
    }

    public Task getBotWarmupTask(TaskStack taskStack, BotClient client)
    {
        return new TaskHunter(taskStack, client);
    }

    public void checkWarmUp()
    {
        if (getGameMode().getPhase() != GameMode.Phase.none)
            return;

        if (BrainOutServer.getInstance().isAutoStart() &&
                BrainOutServer.Controller.getClients().size >= 2)
        {
            warmUp();
        }

    }

    public boolean needRolesForBots()
    {
        return true;
    }

    public void clientMapInitialized(PlayerClient playerClient)
    {

    }

    public boolean reportKillMessage(Client to, Client killer, Client victim)
    {
        return true;
    }

    public boolean allowChat(Client from, Client to)
    {
        return true;
    }

    public void onShuttingDown() {}
}
