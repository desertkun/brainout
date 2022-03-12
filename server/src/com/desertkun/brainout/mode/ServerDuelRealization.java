package com.desertkun.brainout.mode;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.DuelResultsND;
import com.desertkun.brainout.common.enums.data.RankND;
import com.desertkun.brainout.common.msg.server.DuelCompletedMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ActiveFilterComponentData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.mode.payload.DuelPayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateEndGame;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.services.LoginService;
import org.anthillplatform.runtime.services.ProfileService;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;

public class ServerDuelRealization extends ServerRealization<GameModeDuel>
{
    private class DuelRoom
    {
        private Array<Integer> deaths;
        private final String dimension;
        private Set<PlayerClient> duelists;
        private GameModeDuel.DuelState state;
        private boolean started;
        private TimerTask forceSpawnTimer;
        private int initializedCounter;

        public DuelRoom(String dimension, PlayerClient duelist)
        {
            this.dimension = dimension;
            this.duelists = new HashSet<>();
            this.duelists.add(duelist);
            this.state = GameModeDuel.DuelState.waiting;
            this.deaths = new Array<>();
            this.initializedCounter = 0;

            if (Log.INFO) Log.info("New duel room: " + dimension);
        }

        public GameModeDuel.DuelState getState()
        {
            return state;
        }

        public void updated()
        {
            for (PlayerClient duelist : duelists)
            {
                ServerDuelRealization.this.updated(duelist);
            }
        }

        public void setState(GameModeDuel.DuelState state)
        {
            this.state = state;
        }

        public Set<PlayerClient> getDuelists()
        {
            return duelists;
        }

        public void addDuelist(PlayerClient duelist)
        {
            duelists.add(duelist);
        }

        public String getDimension()
        {
            return dimension;
        }

        public boolean removeDualist(PlayerClient duelist)
        {
            duelists.remove(duelist);

            return duelists.isEmpty();
        }

        private int getDeaths(Client client)
        {
            int d = 0;

            for (Integer death : deaths)
            {
                if (client.getId() == death)
                    d++;
            }

            return d;
        }

        public void setStarted()
        {
            this.started = true;
        }

        public boolean isStarted()
        {
            return started;
        }

        public void free()
        {
            if (Log.INFO) Log.info("Duel room has been destroyed: " + dimension);
            duelists.clear();
        }

        private void forceSpawn()
        {
            if (getState() != GameModeDuel.DuelState.spawn)
                return;

            for (PlayerClient duelist : duelists)
            {
                if (!duelist.isAlive())
                {
                    duelist.forceSpawn();
                }
            }
        }

        public void killAll()
        {
            for (PlayerClient duelist :  duelists)
            {
                if (duelist.isAlive())
                {
                    duelist.kill(false, false);
                }
            }
        }

        public void scheduleSpawn()
        {
            switch (getState())
            {
                case waiting:
                case await:
                {
                    break;
                }
                default:
                {
                    return;
                }
            }

            setState(GameModeDuel.DuelState.spawn);

            for (PlayerClient duelist : duelists)
            {
                duelist.requestSpawn();
                ServerDuelRealization.this.updated(duelist);
            }

            forceSpawnTimer = new TimerTask()
            {
                @Override
                public void run()
                {
                    BrainOutServer.PostRunnable(DuelRoom.this::forceSpawn);
                }
            };

            BrainOutServer.Timer.schedule(forceSpawnTimer, 10000);
        }

        public PlayerClient getEnemy(Client client)
        {
            for (PlayerClient duelist : duelists)
            {
                if (duelist != client)
                    return duelist;
            }

            return null;
        }

        public void complete(Client loser, Runnable done)
        {
            setState(GameModeDuel.DuelState.end);
            updated();

            int reward = BrainOutServer.Settings.getPrice("duel");

            LoginService loginService = LoginService.Get();
            ProfileService profileService = ProfileService.Get();

            if (loginService != null && profileService != null)
            {
                JSONObject accounts = new JSONObject();

                for (PlayerClient duelist : duelists)
                {
                    JSONObject account = new JSONObject();
                    JSONObject stats = new JSONObject();
                    account.put("stats", stats);

                    if (duelist == loser)
                    {
                        JSONObject if_ = new JSONObject();
                        if_.put("@func", "--/0");
                        if_.put("@value", reward);
                        stats.put("ru", if_);
                    }
                    else
                    {
                        JSONObject if_ = new JSONObject();
                        if_.put("@func", "++");
                        if_.put("@value", reward);
                        stats.put("ru", if_);
                    }

                    accounts.put(duelist.getAccessTokenAccount(), account);
                }

                profileService.updateMultipleAccountProfiles(
                    loginService.getCurrentAccessToken(),
                    accounts, true, (profileService1, request, result, profiles) ->
                BrainOutServer.PostRunnable(() ->
                {
                    if (Log.INFO) Log.info("Duel completion: " + result.toString());

                    for (PlayerClient duelist : duelists)
                    {
                        boolean nextTry;

                        if (duelist == null)
                            continue;

                        JSONObject updatedProfile = profiles != null ?
                            profiles.optJSONObject(duelist.getAccessTokenAccount()) : null;

                        if (updatedProfile != null)
                        {
                            JSONObject stats = updatedProfile.optJSONObject("stats");

                            if (stats != null)
                            {
                                nextTry = stats.optFloat("ru", 0.0f) >= reward;
                            }
                            else
                            {
                                nextTry = false;
                            }
                        }
                        else
                        {
                            nextTry = false;
                        }

                        duelist.sendTCP(new DuelCompletedMsg(
                            loser.getId(), result == Request.Result.success ? reward : 0,
                            nextTry));
                    }

                    done.run();
                }));
            }
        }

        public void cancelForceSpawn()
        {
            if (forceSpawnTimer != null)
            {
                forceSpawnTimer.cancel();
                forceSpawnTimer = null;
            }
        }

        public int getEnemyDeaths(PlayerClient duelist)
        {
            int count = 0;

            for (Integer death : deaths)
            {
                if (duelist.getId() != death)
                {
                    count++;
                }
            }

            return count;
        }

        public int getMyDeaths(PlayerClient duelist)
        {
            int count = 0;

            for (Integer death : deaths)
            {
                if (duelist.getId() == death)
                {
                    count++;
                }
            }

            return count;
        }

        public void duelistInitialized()
        {
            initializedCounter++;

            if (initializedCounter == 2)
            {
                BrainOutServer.Timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        BrainOutServer.PostRunnable(DuelRoom.this::scheduleSpawn);
                    }
                }, 3000);
            }
        }

        public void duelPlayerDeath(PlayerClient client)
        {
            deaths.add(client.getId());

            for (PlayerClient duelist : duelists)
            {
                int yourDeaths = getMyDeaths(duelist);
                int enemyDeaths = getEnemyDeaths(duelist);

                if (duelist == client)
                {
                    duelist.notify(NotifyAward.score, 0,
                            NotifyReason.duelLost, NotifyMethod.message,
                            new DuelResultsND(yourDeaths, enemyDeaths, BrainOutServer.Settings.getDeathsRequired()));
                }
                else
                {
                    duelist.notify(NotifyAward.score, 0,
                            NotifyReason.duelWon, NotifyMethod.message,
                            new DuelResultsND(yourDeaths, enemyDeaths, BrainOutServer.Settings.getDeathsRequired()));
                }
            }

            if (getDeaths(client) >= BrainOutServer.Settings.getDeathsRequired())
            {
                setState(GameModeDuel.DuelState.await);
                updated();

                BrainOutServer.Timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        BrainOutServer.PostRunnable(() ->
                        {
                            killAll();
                            ServerDuelRealization.this.complete(DuelRoom.this, client);
                        });
                    }
                }, 5000);
            }
            else
            {
                setState(GameModeDuel.DuelState.await);
                updated();

                BrainOutServer.Timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        BrainOutServer.PostRunnable(() ->
                        {
                            killAll();
                            scheduleSpawn();
                        });
                    }
                }, 5000);
            }
        }
    }

    private ObjectMap<String, DuelRoom> rooms;
    private DuelRoom freeRoom;

    public ServerDuelRealization(GameModeDuel gameMode)
    {
        super(gameMode);

        rooms = new ObjectMap<>();
    }

    private void cleanupStuff(Map map)
    {
        Queue<ActiveData> toRemove = new Queue<>();

        for (ObjectMap.Entry<Integer, ActiveData> entry : map.getActives())
        {
            if (entry.value.getCreator().getID().equals("c4-active")
                || entry.value.getCreator().getID().equals("claymore-active"))
            {
                toRemove.addLast(entry.value);
            }
        }

        for (ActiveData activeData : toRemove)
        {
            map.removeActive(activeData, true, true, false);
        }
    }

    @Override
    public void newPlayerClient(PlayerClient playerClient)
    {
        super.newPlayerClient(playerClient);

        ModePayload modePayload = playerClient.getModePayload();

        if (!(modePayload instanceof DuelPayload))
        {
            playerClient.kick("Bad client");
            return;
        }

        DuelPayload duelPayload = ((DuelPayload) modePayload);

        Team team;

        if (freeRoom == null)
        {
            String freeDimension = null;

            Array<String> dimensions = new Array<>();

            for (Map map : Map.All())
            {
                String dimension = map.getDimension();

                if (rooms.containsKey(dimension))
                    continue;

                dimensions.add(dimension);
            }

            if (dimensions.size == 0)
            {
                playerClient.disconnect(DisconnectReason.kicked, "Server is full");
                return;
            }

            freeDimension = dimensions.random();

            freeRoom = new DuelRoom(freeDimension, playerClient);
            rooms.put(freeDimension, freeRoom);

            team = BrainOutServer.Controller.getTeams().get(0);

            duelPayload.setRoom(freeDimension);
        }
        else
        {
            freeRoom.addDuelist(playerClient);
            duelPayload.setRoom(freeRoom.getDimension());

            team = BrainOutServer.Controller.getTeams().get(1);

            freeRoom = null;
        }

        playerClient.teamChanged(team);
        playerClient.setTeam(team);
    }

    @Override
    public void clientReleased(Client client)
    {
        super.clientReleased(client);

        ModePayload modePayload = client.getModePayload();

        if (!(modePayload instanceof DuelPayload))
        {
            return;
        }

        if (!(client instanceof PlayerClient))
        {
            return;
        }

        PlayerClient playerClient = ((PlayerClient) client);
        DuelPayload duelPayload = ((DuelPayload) modePayload);

        DuelRoom room = rooms.get(duelPayload.getRoom());

        if (room == null)
        {
            return;
        }

        if (room.isStarted())
        {
            complete(room, client);
        }
        else
        {
            if (room.removeDualist(playerClient))
            {
                room.free();
                rooms.remove(duelPayload.getRoom());

                if (room == freeRoom)
                {
                    freeRoom = null;
                }
            }
        }
    }

    private void complete(DuelRoom room, Client loser)
    {
        if (Log.INFO) Log.info("Duel room has been completed: " + room.dimension);

        room.complete(loser, () ->
        {
            room.free();
            rooms.remove(room.getDimension());
        });
    }

    @Override
    public void init(PlayState.InitCallback callback)
    {
        super.init(callback);

        for (Map map : Map.All())
        {
            map.getComponents().addComponent(new ActiveFilterComponentData(owner ->
            {
                Client client = BrainOutServer.Controller.getClients().get(owner);
                ModePayload payload = client.getModePayload();
                if (!(payload instanceof DuelPayload))
                    return false;
                DuelPayload duelPayload = ((DuelPayload) payload);
                return duelPayload.getRoom().equals(map.getDimension());
            }));
        }
    }

    @Override
    public void clientMapInitialized(PlayerClient playerClient)
    {
        super.clientMapInitialized(playerClient);

        float ru = playerClient.getStat("ru", 0);
        if (ru < BrainOutServer.Settings.getPrice("duel"))
        {
            playerClient.kick("MENU_NOT_ENOUGH_RU");
            return;
        }

        ModePayload modePayload = playerClient.getModePayload();

        if (modePayload instanceof DuelPayload)
        {
            DuelPayload duelPayload = ((DuelPayload) modePayload);
            DuelRoom room = rooms.get(duelPayload.getRoom());

            if (room != null)
            {
                room.duelistInitialized();
            }
        }
    }

    @Override
    public boolean reportKillMessage(Client to, Client killer, Client victim)
    {
        ModePayload modePayload = to.getModePayload();

        if (modePayload instanceof DuelPayload)
        {
            DuelPayload duelPayload = ((DuelPayload) modePayload);
            DuelRoom room = rooms.get(duelPayload.getRoom());

            if (room != null)
            {
                return room.duelists.contains(killer) || room.duelists.contains(victim);
            }
        }

        return false;
    }

    @Override
    public Set<String> getSuitableDimensions(Client client)
    {
        HashSet<String> h = new HashSet<>();

        if (client == null)
        {
            return h;
        }

        ModePayload modePayload = client.getModePayload();

        if (!(modePayload instanceof DuelPayload))
        {
            return h;
        }

        DuelPayload duelPayload = ((DuelPayload) modePayload);
        h.add(duelPayload.getRoom());
        return h;
    }

    @Override
    public void write(Json json, int owner)
    {
        super.write(json, owner);

        Client client = BrainOutServer.Controller.getClients().get(owner);

        if (client != null)
        {
            ModePayload modePayload = client.getModePayload();

            if (modePayload instanceof DuelPayload)
            {
                DuelPayload duelPayload = ((DuelPayload) modePayload);

                DuelRoom room = rooms.get(duelPayload.getRoom());

                if (room != null)
                {
                    json.writeValue("state", room.state);

                    json.writeArrayStart("dl");
                    for (Integer death : room.deaths)
                    {
                        json.writeValue(death);
                    }
                    json.writeArrayEnd();

                    PlayerClient enemy = room.getEnemy(client);
                    if (enemy != null)
                    {
                        json.writeValue("enemy", enemy.getId());
                    }
                }
            }
        }

        json.writeValue("deaths", BrainOutServer.Settings.getDeathsRequired());
    }

    @Override
    public ModePayload newPlayerPayload(Client playerClient)
    {
        return new DuelPayload(playerClient);
    }

    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        ModePayload modePayload = client.getModePayload();

        if (!(modePayload instanceof DuelPayload))
            return SpawnMode.disallowed;

        DuelPayload duelPayload = ((DuelPayload) modePayload);
        DuelRoom room = rooms.get(duelPayload.getRoom());

        if (room == null)
            return SpawnMode.disallowed;

        switch (room.getState())
        {
            case waiting:
            {
                return SpawnMode.disallowed;
            }
            case spawn:
            {
                return SpawnMode.allowed;
            }
            case active:
            {
                return SpawnMode.allowed;
            }
            default:
            {
                return SpawnMode.disallowed;
            }
        }
    }

    @Override
    public void onClientSpawn(Client client, PlayerData player)
    {
        super.onClientSpawn(client, player);

        if (!getGameMode().isGameActive())
            return;

        ModePayload modePayload = client.getModePayload();

        if (!(modePayload instanceof DuelPayload))
            return;

        DuelPayload duelPayload = ((DuelPayload) modePayload);
        DuelRoom room = rooms.get(duelPayload.getRoom());

        if (room == null)
            return;

        if (room.getState() != GameModeDuel.DuelState.spawn)
            return;

        PlayerOwnerComponent own =
                player.getComponent(PlayerOwnerComponent.class);

        ServerPlayerControllerComponentData ctr =
            player.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

        ctr.setEnabled(false);
        own.setEnabled(false);

        if (isEveryoneSpawned(room))
        {
            room.cancelForceSpawn();
            cleanupStuff(Map.Get(room.getDimension()));
            room.setState(GameModeDuel.DuelState.steady);
            room.updated();

            BrainOutServer.Timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    BrainOutServer.PostRunnable(
                        () -> startRound(room));
                }
            }, MathUtils.random(3000, 5000));

        }
    }

    @Override
    public void onClientDeath(Client client, Client killer, PlayerData playerData, InstrumentInfo info)
    {
        if (!(client instanceof PlayerClient))
            return;

        ModePayload payload = client.getModePayload();

        if (!(payload instanceof DuelPayload))
            return;

        DuelPayload duelPayload = ((DuelPayload) payload);
        DuelRoom room = rooms.get(duelPayload.getRoom());

        if (room == null)
            return;

        switch (room.getState())
        {
            case active:
            {
                room.duelPlayerDeath(((PlayerClient) client));


                break;
            }
        }

    }

    private void startRound(DuelRoom room)
    {
        room.setStarted();
        room.setState(GameModeDuel.DuelState.active);

        PlayerClient done = null;
        PlayerClient completed = null;

        for (PlayerClient duelist : room.duelists)
        {
            updated(duelist);

            if (!duelist.isConnected())
            {
                completed = duelist;
                continue;
            }

            if (!duelist.isAlive())
            {
                done = duelist;
                continue;
            }

            duelist.enablePlayer(true);

            duelist.notify(NotifyAward.score, 0, NotifyReason.duelBegin, NotifyMethod.message,
                new RankND(room.deaths.size + 1, BrainOutServer.Settings.getDeathsRequired()));
        }

        if (done != null)
        {
            room.duelPlayerDeath(done);
        }

        if (completed != null)
        {
            complete(room, completed);
        }
    }

    private boolean isEveryoneSpawned(DuelRoom room)
    {
        for (PlayerClient duelist : room.duelists)
        {
            if (!duelist.isAlive())
                return false;
        }

        return true;
    }

    @Override
    public void release()
    {
        super.release();

        for (ObjectMap.Entry<String, DuelRoom> entry : rooms)
        {
            entry.value.free();
        }

        rooms.clear();
    }

    @Override
    public boolean isComplete(PlayStateEndGame.GameResult gameResult)
    {
        return false;
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        return false;
    }
}
