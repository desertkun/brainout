package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.content.*;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.*;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.PlayStateEndGame;

import java.util.TimerTask;

public class ServerZombieRealization extends ServerRealization<GameModeZombie<ServerZombieRealization>>
{
    private boolean complete;
    private boolean failed;
    private Array<ZombieWave> waves;
    private ObjectMap<Integer, Integer> zombiesKilled;
    private boolean waveActive;
    private float wavePause;
    private int currentWave;
    private int zombiesSpawned;
    private float spawnTimer, check;
    private Team zombieTeam;
    private boolean enablePlayersDeathCheck, nextWaveTriggered;

    public abstract class ZombieItem
    {
        public ZombieItem(JsonValue data)
        {

        }

        public abstract void generate(ConsumableContainer container, String dimension);
    }

    public class DefaultItem extends ZombieItem
    {
        private ConsumableContent item;
        private int amount;

        public DefaultItem(JsonValue data)
        {
            super(data);

            amount = data.getInt("amount", 1);
            item = BrainOutServer.ContentMgr.get(data.getString("item"), ConsumableContent.class);
        }

        @Override
        public void generate(ConsumableContainer container, String dimension)
        {
            container.putConsumable(amount, item.acquireConsumableItem());
        }
    }

    public class InstrumentItem extends ZombieItem
    {
        private InstrumentInfo info;

        public InstrumentItem(JsonValue data)
        {
            super(data);

            info = new InstrumentInfo();
            info.instrument = BrainOutServer.ContentMgr.get(data.getString("item"), Instrument.class);

            if (data.has("skin"))
            {
                info.skin = BrainOutServer.ContentMgr.get(data.getString("skin"), Skin.class);
            }
            else
            {
                info.skin = info.instrument.getDefaultSkin();
            }

            if (data.has("upgrades"))
            {
                JsonValue u = data.get("upgrades");

                for (JsonValue group : u)
                {
                    Upgrade upgrade = BrainOutServer.ContentMgr.get(group.asString(), Upgrade.class);

                    if (upgrade == null)
                        continue;

                    info.upgrades.put(group.name(), upgrade);
                }
            }

        }

        @Override
        public void generate(ConsumableContainer container, String dimension)
        {
            InstrumentData instrumentData = info.instrument.getData(dimension);
            instrumentData.setSkin(info.skin);

            for (ObjectMap.Entry<String, Upgrade> entry : info.upgrades)
            {
                instrumentData.getUpgrades().put(entry.key, entry.value);
            }

            container.putConsumable(1, new InstrumentConsumableItem(instrumentData, dimension));
        }
    }

    private class ZombieKind implements Json.Serializable
    {
        private Player player;
        private Queue<ZombieItem> items;

        public ZombieKind()
        {
            items = new Queue<>();
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            player = BrainOutServer.ContentMgr.get(jsonData.getString("player"), Player.class);

            if (!jsonData.has("items"))
                return;

            for (JsonValue value : jsonData.get("items"))
            {
                String kind = value.getString("kind", "default");
                ZombieItem zombieItem;

                switch (kind)
                {
                    case "instrument":
                    {
                        zombieItem = new InstrumentItem(value);
                        break;
                    }
                    case "default":
                    default:
                    {
                        zombieItem = new DefaultItem(value);
                        break;
                    }
                }

                this.items.addLast(zombieItem);
            }
        }

        public Queue<ZombieItem> getItems()
        {
            return items;
        }

        public Player getPlayer()
        {
            return player;
        }
    }

    private class ZombieWave implements Json.Serializable
    {
        private int amount;
        private int addPerPlayer;
        private int reward;
        private int maxConcurrentAmount;
        private int addMaxConcurrentPerPlayer;
        private float spawnSpeed;
        private float waveTime;
        private Array<ZombieKind> kinds;

        public ZombieWave()
        {
            this.kinds = new Array<>();
        }

        @Override
        public void write(Json json)
        {

        }

        @Override
        public void read(Json json, JsonValue jsonData)
        {
            amount = jsonData.getInt("amount");
            addPerPlayer = jsonData.getInt("addPerPlayer");
            reward = jsonData.getInt("reward");
            spawnSpeed = jsonData.getFloat("spawnSpeed");
            waveTime = jsonData.getFloat("waveTime");
            maxConcurrentAmount = jsonData.getInt("maxConcurrentAmount");
            addMaxConcurrentPerPlayer = jsonData.getInt("addMaxConcurrentPerPlayer");

            for (JsonValue value : jsonData.get("kinds"))
            {
                ZombieKind kind = new ZombieKind();
                kind.read(json, value);
                this.kinds.add(kind);
            }
        }

        public int getAmount()
        {
            return amount;
        }

        public int getReward()
        {
            return reward;
        }

        public int getAddMaxConcurrentPerPlayer()
        {
            return addMaxConcurrentPerPlayer;
        }

        public int getAddPerPlayer()
        {
            return addPerPlayer;
        }

        public float getSpawnSpeed()
        {
            return spawnSpeed;
        }

        public float getWaveTime()
        {
            return waveTime;
        }

        public int getMaxConcurrentAmount()
        {
            return maxConcurrentAmount;
        }

        public Array<ZombieKind> getKinds()
        {
            return kinds;
        }
    }

    public ServerZombieRealization(GameModeZombie<ServerZombieRealization> gameMode)
    {
        super(gameMode);

        this.waves = new Array<>();
        this.zombiesKilled = new ObjectMap<>();
        this.complete = false;
        this.waveActive = false;
        this.wavePause = 20;
        this.currentWave = -1;
        this.spawnTimer = 0;
        this.failed = true;
    }

    @Override
    public void init(PlayState.InitCallback callback)
    {
        super.init(callback);

        Map map = Map.GetDefault();
        map.setCustom("ambient-light", "0.2");
    }

    @Override
    protected void warmUpComplete()
    {
        for (Client client : BrainOutServer.Controller.getClients().values())
        {
            if (client.isInitialized())
            {
                client.kill();
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
        BrainOutServer.PostRunnable(this::updated);
        BrainOutServer.Controller.updateRoomSettings();

        nextWave();
    }

    private void nextWave()
    {
        nextWaveTriggered = false;

        Map map = Map.GetDefault();

        if (map != null)
        {
            Team playersTeam = getPlayersTeam();

            if (playersTeam != null)
            {
                ActiveData suppliesSpawn = map.getActiveNameIndex().get("supplies");
                Active ammoBox = BrainOutServer.ContentMgr.get("ammobox-active", Active.class);

                if (ammoBox != null)
                {
                    ActiveData box = ammoBox.getData(map.getDimension());
                    box.setPosition(suppliesSpawn.getX(), suppliesSpawn.getY());
                    box.setTeam(playersTeam);

                    map.addActive(map.generateServerId(), box, true, true);
                }
            }
        }

        zombiesKilled.clear();

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (!(client instanceof PlayerClient))
                continue;

            PlayerClient playerClient = ((PlayerClient) client);
            playerClient.setSpectator(false);
        }

        waveActive = false;
        zombiesSpawned = 0;

        currentWave++;
        if (currentWave >= waves.size)
        {
            complete();
            return;
        }

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (!(client instanceof PlayerClient))
                continue;

            PlayerClient playerClient = ((PlayerClient) client);

            if (playerClient.isAlive())
            {
                playerClient.kill();
            }
        }

        getGameMode().resetEndTime();
        getGameMode().setTimer(wavePause, this::startWave);
        updated();
    }

    private Team getPlayersTeam()
    {
        for (Team team : BrainOutServer.Controller.getTeams())
        {
            if (team instanceof SpectatorTeam)
                continue;

            if (team == zombieTeam)
                continue;

            return team;
        }

        return null;
    }

    private ZombieWave getCurrentWave()
    {
        if (currentWave < 0 || currentWave >= waves.size)
            return null;

        return waves.get(currentWave);
    }

    private void complete()
    {
        this.failed = false;
        this.complete = true;
    }

    private void startWave()
    {
        waveActive = true;
        enablePlayersDeathCheck = true;
        checkDeadPlayers();

        ZombieWave currentWave = getCurrentWave();

        if (currentWave == null)
        {
            complete();
            return;
        }

        getGameMode().setEndTime((long)(currentWave.getWaveTime()));
        updated();
    }

    @Override
    public boolean canDropConsumable(Client playerClient, ConsumableItem item)
    {
        return false;
    }

    @Override
    public boolean isDeathDropEnabled(PlayerData playerData)
    {
        return false;
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        gameResult.setTeamWon(zombieTeam);

        return true;
    }

    @Override
    public float calculateDamage(Team receiverTeam, Team senderTeam, int receiverId, int senderId, float dmg)
    {
        if (senderTeam == zombieTeam)
        {
            // zombies make twice as less damage as players
            dmg /= 2.0f;
        }

        return dmg;
    }

    @Override
    public boolean isComplete(PlayStateEndGame.GameResult gameResult)
    {
        if (!complete)
            return false;

        if (failed)
        {
            gameResult.setTeamWon(zombieTeam);
        }
        else
        {
            for (Team team : BrainOutServer.Controller.getTeams())
            {
                if (team instanceof SpectatorTeam)
                    continue;

                if (team == zombieTeam)
                    continue;

                gameResult.setTeamWon(team);
                break;
            }
        }

        return true;
    }

    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        zombieTeam = BrainOutServer.ContentMgr.get(jsonData.getString("zombieTeam"), Team.class);
        wavePause = jsonData.getFloat("wavePause");

        for (JsonValue wave : jsonData.get("waves"))
        {
            ZombieWave newWave = new ZombieWave();
            newWave.read(json, wave);
            this.waves.add(newWave);
        }
    }

    private int countPlayers()
    {
        int cnt = 0;

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (!client.isInitialized())
                continue;

            if (client.getTeam() instanceof SpectatorTeam)
                continue;

            cnt++;
        }

        return cnt;
    }

    private int countAlivePlayers()
    {
        int cnt = 0;

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (!client.isInitialized())
                continue;

            if (client.getTeam() instanceof SpectatorTeam)
                continue;

            if (client.isAlive())
                cnt++;
        }

        return cnt;
    }

    private int countAliveZombies()
    {
        int cnt = 0;

        for (Map map : Map.All())
        {
            cnt += map.countActivesForTag(Constants.ActiveTags.PLAYERS,
                activeData -> activeData.isAlive() && activeData.getComponent(ZombieComponentData.class) != null);
        }

        return cnt;
    }

    @Override
    public void write(Json json, int owner)
    {
        super.write(json, owner);

        json.writeValue("zombieTeam", zombieTeam.getID());
        json.writeValue("waveCount", waves.size);
        json.writeValue("wavePause", wavePause);
        json.writeValue("waveActive", waveActive);
        json.writeValue("currentWave", currentWave);
        json.writeValue("zombiesSpawned", zombiesSpawned);
        json.writeValue("zombiesAlive", countAliveZombies());

        json.writeArrayStart("playersAlive");

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            if (client.getTeam() instanceof SpectatorTeam)
                continue;

            if (client.isAlive())
            {
                json.writeValue(entry.key);
            }
        }

        json.writeArrayEnd();

        ZombieWave wave = getCurrentWave();
        if (wave != null)
        {
            json.writeValue("zombiesAmount", calculateZombiesAmount(wave));
        }
    }

    private int calculateZombiesAmount(ZombieWave wave)
    {
        return wave.getAmount() + wave.getAddPerPlayer() * countPlayers();
    }

    private int calculateMaxConcurrentAmount(ZombieWave wave)
    {
        return wave.getMaxConcurrentAmount() + wave.getAddMaxConcurrentPerPlayer() * countPlayers();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        ZombieWave wave = getCurrentWave();

        if (wave != null)
        {
            if (waveActive)
            {
                check -= dt;

                if (check < 0)
                {
                    check = 1.0f;

                    int zombiesAlive = countAliveZombies();

                    if (zombiesAlive < calculateMaxConcurrentAmount(wave) && zombiesSpawned < calculateZombiesAmount(wave))
                    {
                        spawnTimer -= 1.0f;

                        if (spawnTimer < 0)
                        {
                            spawnAZombie(wave);
                            spawnTimer = 60.f / wave.spawnSpeed;
                        }
                    }

                    if (checkZombies())
                    {
                        updated();
                    }
                }
            }
        }
    }

    private boolean checkZombies()
    {
        if (nextWaveTriggered)
            return false;

        if (complete)
            return false;

        ZombieWave wave = getCurrentWave();

        if (wave == null)
            return false;

        if (countAliveZombies() == 0 && zombiesSpawned >= calculateZombiesAmount(wave))
        {
            int totalKills = 0;

            for (ObjectMap.Entry<Integer, Integer> entry : zombiesKilled)
            {
                totalKills += entry.value;
            }

            if (totalKills > 0)
            {
                for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
                {
                    int reward = (int) (
                            ((float) zombiesKilled.get(entry.key, 0) / (float) totalKills) * (float) wave.getReward()
                    );

                    Client client = entry.value;

                    client.addStat("candies", reward);
                    client.addScore(reward, true);
                    client.notify(NotifyAward.candies, reward,
                            NotifyReason.zombieWaveCompleted, NotifyMethod.message, null);
                }
            }

            nextWaveTriggered = true;

            BrainOutServer.Timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                   BrainOutServer.PostRunnable(ServerZombieRealization.this::nextWave);
                }
            }, 3000);

            return true;
        }

        return false;
    }

    private void spawnAZombie(ZombieWave wave)
    {
        ZombieKind randomKind = wave.getKinds().random();

        Array<ActiveData> spawnables = new Array<>();

        for (Map map : Map.All())
        {
            map.countActivesForTag(Constants.ActiveTags.SPAWNABLE, activeData ->
            {
                if (activeData.getTeam() != zombieTeam)
                    return false;

                SubPointComponentData sp = activeData.getComponent(SubPointComponentData.class);

                if (sp == null)
                    return false;

                spawnables.add(activeData);

                return true;
            });
        }

        ActiveData spawnAt = spawnables.random();

        if (spawnAt == null)
            return;

        Map map = Map.Get(spawnAt.getDimension());

        if (map == null)
            return;

        PlayerData playerData = (PlayerData)randomKind.getPlayer().getData(map.getDimension());

        playerData.setTeam(zombieTeam);

        PlayerOwnerComponent ownerComponent = new PlayerOwnerComponent(playerData);
        PlayerRemoteComponent remoteComponent = new PlayerRemoteComponent(playerData);

        playerData.addComponent(ownerComponent);
        playerData.addComponent(remoteComponent);
        playerData.addComponent(new ServerDestroyCallbackComponentData(playerData,
            (co) -> BrainOutServer.PostRunnable(() -> zombieKilled(((PlayerData) co)))));

        float spawnX = spawnAt.getX(), spawnY = spawnAt.getY() + 1.5f;
        playerData.setPosition(spawnX, spawnY);

        for (ZombieItem item : randomKind.getItems())
        {
            item.generate(ownerComponent.getConsumableContainer(), map.getDimension());
        }

        ServerPlayerControllerComponentData ctl =
            playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

        if (ctl != null)
        {
            ctl.selectFirstInstrument(ownerComponent);
        }

        remoteComponent.setCurrentInstrument(ownerComponent.getCurrentInstrument());
        playerData.setCurrentInstrument(ownerComponent.getCurrentInstrument());

        map.addActive(map.generateServerId(), playerData, true, true, ActiveData.ComponentWriter.TRUE);

        zombiesSpawned += 1;

        updated();
    }

    private void zombieKilled(PlayerData zombie)
    {
        ActiveData.LastHitInfo lastHitInfo = zombie.getLastHitInfo();

        if (lastHitInfo != null)
        {
            zombiesKilled.put(lastHitInfo.hitterId, zombiesKilled.get(lastHitInfo.hitterId, 0) + 1);
        }

        checkZombies();
        updated();
    }

    @Override
    public void finished()
    {
        BrainOutServer.Controller.setSpeed(0.5f);

        BrainOut.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() -> BrainOutServer.Controller.setSpeed(1f));
            }
        }, 4000);
    }

    @Override
    public void clientCompletelyInitialized(Client client)
    {
        super.clientCompletelyInitialized(client);

        if (!client.isAlive())
        {
            client.requestSpawn();
        }

        updated();
    }

    @Override
    public boolean forceWeaponAutoLoad()
    {
        return true;
    }

    @Override
    public void clientReleased(Client client)
    {
        super.clientReleased(client);

        updated();
    }

    @Override
    public ModePayload newPlayerPayload(Client playerClient)
    {
        return null;
    }

    @Override
    public void onClientDeath(Client client, Client killer, PlayerData playerData, InstrumentInfo info)
    {
        super.onClientDeath(client, killer, playerData, info);

        if (waveActive)
        {
            if (getGameMode().isGameActive() && client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);
                playerClient.setSpectator(true);
            }
        }

        updated();
        checkDeadPlayers();
    }

    private void checkDeadPlayers()
    {
        if (!waveActive)
            return;

        if (enablePlayersDeathCheck)
        {
            BrainOutServer.PostRunnable(() ->
            {
                if (countAlivePlayers() == 0)
                {
                    failed();
                }
            });
        }
    }

    private void failed()
    {
        failed = true;
        complete = true;
    }

    @Override
    public SpawnMode canSpawn(Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public boolean needRolesForBots()
    {
        return false;
    }
}
