package com.desertkun.brainout.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.ClientList;
import com.desertkun.brainout.client.ConnectionList;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.msg.client.SimpleMsg;
import com.desertkun.brainout.common.msg.server.*;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.GlobalConflict;
import com.desertkun.brainout.content.Levels;
import com.desertkun.brainout.content.SpectatorTeam;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.content.upgrades.UpgradeChain;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ActiveFilterComponentData;
import com.desertkun.brainout.data.components.HealthComponentData;
import com.desertkun.brainout.data.components.ServerTeamVisibilityComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.Spawnable;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.online.Preset;
import com.desertkun.brainout.online.RoomSettings;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;
import com.desertkun.brainout.plugins.Plugin;
import com.desertkun.brainout.server.console.Console;
import com.desertkun.brainout.server.mapsource.MapSource;
import com.desertkun.brainout.utils.*;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.services.GameService;
import org.json.JSONObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.LinkedHashSet;
import java.util.Set;

public class ServerController extends Controller implements EventReceiver
{
    private ClientList clients;
    private ConnectionList connections;
    private ActiveData.ComponentWriter mapComponentWriter;
    private Console console;
    private ServerReliableManager reliableManager;
    private Array<Plugin> plugins;
    private Array<Team> teams;
    private MapSource mapSource;
    private ObjectMap<String, String> globalDefines;
    private ObjectMap<String, Integer> ownerKeys;

    private Array<Content> contentIndex;
    private ObjectMap<String, Integer> contentIndexMap;

    private TimeoutFlag pingTimer, updateInfoTimer;
    private ClientList.Matching pingMatching;
    private float waveTimer;

    private Queue<Array<Client>> waves;

    private DeathsRate deathsRate;
    private RoomSettings initRoomSettings;

    private ObjectMap<String, Levels> levels;
    private long time;
    private MapSource.Settings currentSettings;
    private boolean shutdownRequired;
    private ObjectMap<String, Team> clanTeams;
    private ObjectMap<String, ClanPartyMember> clanPartyMembers;

    public void next(PlayState.InitCallback callback)
    {
        currentSettings = mapSource.next();
        mapSource.processConditions(currentSettings);
        currentSettings.init(callback);
    }

    public Array<Content> getContentIndex()
    {
        return contentIndex;
    }

    public void updateContentIndex()
    {
        contentIndex.clear();

        BrainOutServer.ContentMgr.queryContentGen(Content.class, content ->
        {
            if ("server".equals(content.getTag()))
            {
                return;
            }

            if (content instanceof UpgradeChain.ChainedUpgrade)
            {
                return;
            }

            if (content instanceof Upgrade)
            {
                return;
            }

            if (content instanceof UpgradeChain)
            {
                return;
            }

            contentIndex.add(content);
        });

        if (Log.INFO) Log.info("Updated global index (" + contentIndex.size + " items)");

        contentIndexMap.clear();

        int i = 0;
        for (Content c : contentIndex)
        {
            contentIndexMap.put(c.getID(), i);
            i++;
        }
    }

    public int getContentIndexFor(Content content)
    {
        return contentIndexMap.get(content.getID(), -1);
    }

    @Override
    public <T extends Content> T getContentFromIndex(int index, Class<T> clazz)
    {
        if (contentIndex == null)
        {
            return null;
        }

        if (index >= contentIndex.size)
        {
            return null;
        }
        Content c = contentIndex.get(index);
        if (clazz.isInstance(c))
        {
            return ((T) c);
        }

        return null;
    }

    public Array<ServerMap> loadMaps(ServerSettings.MapConditions mapConditions, boolean init)
    {
        return mapSource.loadMaps(mapConditions, init);
    }

    public MapSource.Settings getCurrentSettings()
    {
        return currentSettings;
    }

    public void gracefulShutdown()
    {
        this.shutdownRequired = true;
    }

    public Set<String> getSuitableDimensions(Client client)
    {
        GameMode gameMode = getGameMode();

        if (gameMode == null)
        {
            Set<String> d = new LinkedHashSet<>();

            for (ObjectMap.Entry<String, ServerMap> entry : Map.SafeAll(ServerMap.class))
            {
                if (client instanceof PlayerClient)
                {
                    if (entry.value.isPersonalRequestOnly() &&
                        !entry.value.suitableForPersonalRequestFor(((PlayerClient) client).getAccount()))
                    {
                        continue;
                    }
                }

                d.add(entry.value.getDimension());
            }

            return d;
        }

        ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());

        //noinspection unchecked
        return serverRealization.getSuitableDimensions(client);
    }

    public void sendRemotePlayers()
    {
        BrainOutServer.PostRunnable(() ->
        {
            for (ObjectMap.Entry<Integer, Client> entry : new ObjectMap.Entries<>(getClients()))
            {
                if (entry.value instanceof PlayerClient)
                {
                    ((PlayerClient) entry.value).updateRemotePlayers();
                }
            }
        });
    }

    public void sendRemotePlayers(final Client sendWho)
    {
        BrainOutServer.PostRunnable(() ->
        {
            for (ObjectMap.Entry<Integer, Client> entry : new ObjectMap.Entries<>(getClients()))
            {
                if (entry.value instanceof PlayerClient)
                {
                    ((PlayerClient) entry.value).updateRemotePlayers(sendWho);
                }
            }
        });
    }

    public class ClanPartyMember
    {
        private String clanId;

        public ClanPartyMember(JSONObject data)
        {
            JSONObject profile = data.optJSONObject("profile");

            if (profile != null)
            {
                read(profile);
            }
        }

        private void read(JSONObject profile)
        {
            clanId = profile.optString("clan-id");
        }

        public String getClanId()
        {
            return clanId;
        }
    }

    public class DeathsRate
    {
        private int rate;
        private int deaths;
        private float timer;

        public void update(float dt)
        {
            timer -= dt;

            if (timer < 0)
            {
                rate = deaths;

                deaths = 0;
                timer = ServerConstants.Controller.DEATHS_PERIOD;
            }
        }

        public void onDeath()
        {
            deaths++;
        }


        public float getDeathsRate()
        {
            return rate / ServerConstants.Controller.DEATHS_PERIOD;
        }
    }

    public enum RespawnKind
    {
        immedeately,
        waves,
        dynamicWaves
    }

    @SuppressWarnings("Convert2Lambda")
    public ServerController(BrainOut brainOut)
    {
        super(brainOut);

        contentIndex = new Array<>();
        contentIndexMap = new ObjectMap<>();
        console = new Console();
        clients = new ClientList(this);
        connections = new ConnectionList();
        shutdownRequired = false;
        ownerKeys = new ObjectMap<>();

        pingTimer = new TimeoutFlag(0);
        updateInfoTimer = new TimeoutFlag(0);

        pingMatching = Client::isTeamSelected;

        reliableManager = new ServerReliableManager();
        waveTimer = 0;
        teams = new Array<>();
        clanTeams = new ObjectMap<>();
        deathsRate = new DeathsRate();

        waves = new Queue<>();
        globalDefines = new ObjectMap<>();

        mapComponentWriter = new ActiveData.ComponentWriter()
        {
            @Override
            public boolean canSend(int owner, Data data, Component component)
            {
                return !(data instanceof PlayerData) ||
                        component instanceof PlayerRemoteComponent ||
                        component instanceof HealthComponentData;
            }
        };

        plugins = new Array<>();
        levels = new ObjectMap<>();
    }

    public ObjectMap<String, String> getGlobalDefines()
    {
        return globalDefines;
    }

    public boolean isRatingEnabled(boolean shouldBeActive)
    {
        GameMode gameMode = getGameMode();

        if (gameMode != null)
        {
            if (shouldBeActive && !gameMode.isGameActive())
            {
                return false;
            }
        }

        return BrainOutServer.Settings.isRatingEnabled();
    }

    public void init()
    {
        reset();

        BrainOut.EventMgr.subscribe(Event.ID.setBlock, this);
        BrainOut.EventMgr.subscribe(Event.ID.activeAction, this);
        BrainOut.EventMgr.subscribe(Event.ID.activeChangeDimension, this);
        BrainOut.EventMgr.subscribe(Event.ID.componentUpdated, this);
        BrainOut.EventMgr.subscribe(Event.ID.playStateUpdated, this);
        BrainOut.EventMgr.subscribe(Event.ID.blockHitConfirmation, this);

        setCurrentTime();
    }

    public RoomSettings getRoomSettings()
    {
        RoomSettings settings = new RoomSettings();

        PlayState ps = getPlayState();

        if (ps instanceof ServerPSGame)
        {
            ServerPSGame playState = ((ServerPSGame) ps);

            if (playState.getCurrentMap() != null)
                settings.getMap().define(playState.getCurrentMap().getMapName());

            if (playState.getCurrentMode() != null)
                settings.getMode().define(playState.getCurrentMode().name);

            GameMode mode = playState.getMode();
            if (mode.isAboutToEnd())
            {
                settings.setState("ending");
            }
            else
            {
                settings.setState("active");
            }

            settings.setWarmup(playState.getPhase() == GameMode.Phase.warmUp ? "true" : "false");
        }

        if (getInitRoomSettings() != null)
        {
            settings.setPreset(getInitRoomSettings().getPreset());

            if (getInitRoomSettings().getSubscribers() >= 0)
            {
                settings.setSubscribers(getInitRoomSettings().getSubscribers());
            }

            if (getInitRoomSettings().getNewbie() != null)
            {
                settings.setNewbie(getInitRoomSettings().getNewbie());
            }
        }

        settings.setLevel(getAverageLevel());

        return settings;
    }

    public void reset()
    {
        for (ObjectMap.Entry<String, String> entry : BrainOutServer.Settings.getLevels())
        {
            levels.put(entry.key, ((Levels) BrainOut.ContentMgr.get(entry.value)));
        }

        teams.clear();

        PlayState ps = getPlayState();

        if (ps instanceof ServerPSGame)
        {
            ServerPSGame serverPSGame = ((ServerPSGame) ps);
            teams.addAll(serverPSGame.getTeams());
        }

        clients.reset(teams);
    }

    public Levels getLevels(String kind)
    {
        return levels.get(kind);
    }

    public ClientList getClients()
    {
        return clients;
    }

    public ConnectionList getConnections()
    {
        return connections;
    }

    public void initSettings(ServerSettings settings)
    {
        for (String packageName: settings.getBaseConditions().packages)
        {
            BrainOut.PackageMgr.registerPackage(packageName);
        }

        BrainOut.PackageMgr.setDefine("server", "true");

        for (ObjectMap.Entry<String, String> entry : settings.getBaseConditions().defines)
        {
            BrainOut.PackageMgr.setDefine(entry.key, entry.value);
        }

        for (ObjectMap.Entry<String, String> entry : getGlobalDefines())
        {
            BrainOut.PackageMgr.setDefine(entry.key, entry.value);
        }
    }

    public void resetSettings()
    {
        BrainOut.ContentMgr.unloadAllContent();
        applyInitRoomSettings();

        initSettings(BrainOutServer.Settings);
    }

    public void updatePlayState(PlayState playState)
    {
        setPlayState(playState);

        playStateChanged();
    }

    public void playStateChanged()
    {
        for (ObjectMap.Entry<Integer, Client> entry: new ObjectMap.Entries<>(BrainOutServer.Controller.getClients()))
        {
            Client client = entry.value;
            if (client instanceof PlayerClient)
            {
                ((PlayerClient) client).sendTCP(
                        new PlayStateChangedMsg(getPlayState(), client.getId()));
            }
        }
    }

    public void playStateUpdated()
    {
        for (ObjectMap.Entry<Integer, Client> entry: new ObjectMap.Entries<>(BrainOutServer.Controller.getClients()))
        {
            Client client = entry.value;
            if (client instanceof PlayerClient)
            {
                ((PlayerClient) client).sendTCP(
                        new PlayStateUpdatedMsg(getPlayState(), client.getId()));
            }
        }
    }

    public PlayState setPlayState(PlayState.InitCallback callback, PlayState.ID playStateId)
    {
        for (Plugin plugin : plugins)
        {
            plugin.reset();
        }

        setCurrentTime();

        try
        {
            setPlayState(playStateId);
        }
        catch (Exception e)
        {
            if (Log.ERROR) Log.error("Failed to set playState: " + e.getMessage());
            e.printStackTrace();
        }

        if (getPlayState() == null)
        {
            if (Log.ERROR) Log.error("Failed to set playState");

            return null;
        }

        getPlayState().init((success) ->
        {
            if (callback != null)
            {
                callback.done(success);
            }

            if (success)
            {
                playStateChanged();
            }
        });

        return getPlayState();
    }

    public interface MapSaver
    {
        void postpone(Runnable runnable);
    }

    public interface MapSaveResult
    {
        byte[] serialize();
    }

    private static class MapSaverHandler
    {
        public byte[] compressedDefault;
        public byte[] verify;
        public MapSerializer.MapHeader header = new MapSerializer.MapHeader();
        public int offset = 0;
    }

    public MapSaveResult saveAll(
            final Set<String> dimensions,
            final ActiveData.ComponentWriter componentWriter,
            boolean addSignature, int owner,
            final MapSaver saver)
    {
        return saveAll(dimensions, componentWriter, addSignature, owner, "default", saver);
    }


    public MapSaveResult saveAll(
        final Set<String> dimensions,
        final ActiveData.ComponentWriter componentWriter,
        boolean addSignature, int owner,
        String defaultDimension,
        final MapSaver saver)
    {
        try
        {
            final MapSaverHandler handler = new MapSaverHandler();
            handler.header.setVersion(Constants.Maps.V);

            Json json = new Json();

            final Map defaultMap;

            {
                Map d = Map.Get(defaultDimension);

                if (d == null)
                {
                    for (Map map : Map.SafeAll())
                    {
                        if (!dimensions.contains(map.getDimension()))
                        {
                            continue;
                        }

                        d = map;
                        break;
                    }

                    if (d == null)
                    {
                        return null;
                    }
                }

                defaultMap = d;
            }

            saver.postpone(() ->
            {
                StringWriter stringWriter = new StringWriter();

                {
                    BrainOut.R.tag(json);

                    json.setWriter(stringWriter);
                    json.writeArrayStart();

                    ActiveFilterComponentData filter =
                        defaultMap.getComponents().getComponent(ActiveFilterComponentData.class);

                    if (filter == null || filter.filters(owner))
                    {
                        json.writeObjectStart();
                        defaultMap.write(json, componentWriter, owner);
                        json.writeObjectEnd();
                    }

                    json.writeArrayEnd();
                }

                String dataDefault = stringWriter.toString();

                if (addSignature)
                {
                    handler.verify = HashUtils.Verify(ServerConstants.Maps.MAP_KEY, dataDefault.getBytes());

                    if (handler.verify == null)
                    {
                        throw new RuntimeException("No verify!");
                    }
                }

                stringWriter.flush();
                handler.compressedDefault = Compressor.Compress(dataDefault);
                handler.header.setBodySize(handler.compressedDefault.length);
            });

            MapSerializer.MapHeader header = handler.header;

            Queue<String> extensionNames = new Queue<>();
            Queue<String> mapExtensionNames = new Queue<>();

            handler.offset = 0;

            ObjectMap<String, byte[]> mapExtensions = new ObjectMap<>();

            for (String d : dimensions)
            {
                final String dimension = d;
                if (dimension.equals(defaultMap.getDimension()))
                    continue;

                saver.postpone(() ->
                {
                    Map map = Map.Get(dimension);

                    if (map == null)
                        return;

                    ActiveFilterComponentData filter =
                            map.getComponents().getComponent(ActiveFilterComponentData.class);

                    if (filter != null && !filter.filters(owner))
                        return;

                    StringWriter extWriter = new StringWriter();
                    json.setWriter(extWriter);

                    json.writeObjectStart();
                    map.write(json, componentWriter, owner);
                    json.writeObjectEnd();

                    byte[] ex = Compressor.Compress(extWriter.toString());
                    extWriter.flush();

                    if (ex != null)
                    {
                        header.addExtension(dimension, handler.offset, ex.length, true);
                        mapExtensions.put(dimension, ex);
                        handler.offset += ex.length;
                        mapExtensionNames.addLast(dimension);
                    }
                });
            }

            saver.postpone(() ->
            {
                for (ObjectMap.Entry<String, byte[]> entry : defaultMap.getExtensions())
                {
                    header.addExtension(entry.key, handler.offset, entry.value.length);
                    handler.offset += entry.value.length;
                    extensionNames.addLast(entry.key);
                }
            });

            return () ->
            {
                String headerJson = new Json().toJson(header);
                byte[] compressedHeader = Compressor.Compress(headerJson);

                if (compressedHeader == null)
                {
                    return null;
                }

                ByteArrayOutputStream os = new ByteArrayOutputStream();

                try
                {
                    if (handler.verify != null)
                    {
                        os.write(handler.verify);
                    }
                    os.write(Constants.Maps.MAGIC.getBytes());

                    byte[] headerLength = ByteBuffer.allocate(4).putInt(compressedHeader.length).array();
                    os.write(headerLength);
                    os.write(compressedHeader);
                    os.write(handler.compressedDefault);

                    for (String extName : mapExtensionNames)
                    {
                        byte[] ex = mapExtensions.get(extName);
                        os.write(ex);
                    }

                    for (String extName : extensionNames)
                    {
                        byte[] ex = defaultMap.getExtension(extName);
                        os.write(ex);
                    }

                    BrainOut.ScheduleGC();
                }
                catch (IOException e)
                {
                    // ignore
                }

                handler.compressedDefault = null;
                handler.header.getExtensions().clear();
                handler.header = null;

                mapExtensions.clear();
                extensionNames.clear();
                mapExtensionNames.clear();

                byte[] out = os.toByteArray();
                os.reset();

                return out;
            };
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public boolean mapExists(String fileName)
    {
        FileHandle fileHandle = Gdx.files.absolute(fileName);
        return fileHandle.exists();
    }

    public boolean saveAll(String fileName)
    {
        try
        {
            FileHandle fileHandle = Gdx.files.absolute(fileName);

            StringWriter stringWriter = new StringWriter();
            Json json = new Json();

            BrainOut.R.tag(json);

            json.setWriter(stringWriter);
            json.writeArrayStart();

            for (Map map : Map.SafeAll())
            {
                json.writeObjectStart();
                map.write(json, ActiveData.ComponentWriter.TRUE, -1);
                json.writeObjectEnd();
            }

            json.writeArrayEnd();

            String data = stringWriter.toString();
            byte[] compressed = Compressor.Compress(data);
            if (compressed == null)
                return false;

            fileHandle.write(new ByteArrayInputStream(compressed, 0, compressed.length), false);

            if (Log.INFO) Log.info("Map saved to " + fileName);

            return true;
        }
        catch (Exception e)
        {
            ExceptionHandler.handle(e);

            if (Log.ERROR) Log.error("Failed to save map:" + e.getMessage());

            return false;
        }
    }

    public Array<ServerMap> loadMaps(InputStream inputStream)
    {
        return loadMaps(inputStream, false);
    }

    public Array<ServerMap> loadMaps(InputStream inputStream, boolean verify)
    {
        if (Log.INFO) Log.info("Loading maps from stream...");

        try
        {
            Array<ServerMap> maps;

            try
            {
                maps = MapSerializer.LoadMaps(inputStream, ServerMap.class, ServerConstants.Maps.MAP_KEY);
            }
            catch (OutOfMemoryError e)
            {
                // well, collect the garbage and try once again
                Garbage.gc();

                maps = MapSerializer.LoadMaps(inputStream, ServerMap.class, ServerConstants.Maps.MAP_KEY);
            }

            if (maps == null)
            {
                if (Log.ERROR) Log.error("Maps load error!");
                return null;
            }

            for (ServerMap map : maps)
            {
                if (Log.INFO) Log.info("Map loaded (" + map.getBlocks().getBlockWidth() +
                        "x" + map.getBlocks().getBlockHeight() + " blocks) dimension: " + map.getDimension() + ".");
            }

            return maps;
        }
        catch (Exception e)
        {
            if (Log.ERROR) Log.error("Failed to load map:" + e.getMessage());
            return null;
        }
    }

    public Array<ServerMap> loadMaps(String mapFile, String verify)
    {
        if (Log.INFO) Log.info("Loading maps from " + mapFile + "...");
        try
        {
            Array<ServerMap> maps;

            try
            {
                maps = BrainOut.loadMapsFromFile(mapFile, ServerMap.class, verify);
            }
            catch (OutOfMemoryError e)
            {
                // well, collect the garbage and try once again
                Garbage.gc();

                maps = BrainOut.loadMapsFromFile(mapFile, ServerMap.class, verify);
            }

            for (ServerMap map : maps)
            {
                if (Log.INFO) Log.info("Map loaded (" + map.getBlocks().getBlockWidth() +
                        "x" + map.getBlocks().getBlockHeight() + " blocks) dimension: " + map.getDimension() + ".");
            }

            return maps;
        }
        catch (Exception e)
        {
            ExceptionHandler.handle(e);

            if (Log.ERROR) Log.error("Failed to load map:" + e.getMessage());
            return null;
        }
    }

    public ServerMap createMap(int mWidth, int mHeight, String dimension)
    {
        if (mWidth < 0 || mHeight < 0 || mWidth > 32 || mHeight > 8)
            return null;

        try
        {
            ServerMap serverMap = new ServerMap(dimension, mWidth, mHeight);

            if (Log.INFO) Log.info("Map created");

            return serverMap;
        }
        catch (Exception e)
        {
            ExceptionHandler.handle(e);

            if (Log.ERROR) Log.error("Failed to create map:" + e.getMessage());
            return null;
        }
    }

    public void removeOwnerKey(int owner)
    {
        for (ObjectMap.Entry<String, Integer> entry : this.ownerKeys)
        {
            if (entry.value == owner)
            {
                this.ownerKeys.remove(entry.key);
                return;
            }
        }
    }

    public int getOwnerForKey(String key)
    {
        if (key == null)
            return -1;

        return this.ownerKeys.get(key, -1);
    }

    public String addOwnerKey(int owner)
    {
        String key = StringFunctions.generate(16);

        this.ownerKeys.put(key, owner);

        return key;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        getClients().update(dt);

        if (pingTimer.isGone())
        {
            pingPlayers();
            pingTimer.setValue(ServerConstants.Clients.PING_TIME);
        }

        if (updateInfoTimer.isGone())
        {
            updatePlayersInfo();
            updateInfoTimer.setValue(ServerConstants.Clients.UPDATE_INFO_TIME);
        }

        reliableManager.update();

        deathsRate.update(dt);

        ServerSettings ss = BrainOutServer.Settings;

        switch (ss.getRespawnKind())
        {
            case waves:
            case dynamicWaves:
            {
                waveTimer -= dt;

                if (waveTimer <= 0)
                {
                    waveTimer = getNextWaveTime();

                    doRespawn();
                }

                break;
            }
        }
    }

    private float getNextWaveTime()
    {
        ServerSettings ss = BrainOutServer.Settings;

        switch (ss.getRespawnKind())
        {
            case dynamicWaves:
            {
                float deathsPerSecond = deathsRate.getDeathsRate();
                return deathsPerSecond * ss.getRespawnWaveRate();
            }
            case waves:
            default:
            {
                return ss.getRespawnWaveTime();
            }
        }
    }

    private void updatePlayersInfo()
    {
        final Array<ClientsInfo.PingInfo> infos = new Array<>();

        getClients().foreach(pingMatching, client ->
            infos.add(new ClientsInfo.PingInfo(client.getId(), client.getPing(),
                client.getScore(), client.getKills(), client.getDeaths(), client.getTeam().getID(),
                client.getLevel(Constants.User.LEVEL, 1), client.getRights())));

        getClients().sendUDP(new ClientsInfo(infos), pingMatching);
    }

    private void pingPlayers()
    {
        // ping them
        getClients().sendUDP(new PingMsg(System.currentTimeMillis()), pingMatching);
    }

    public void applyAndSendSlowMo(float slowmo)
    {
        applySlowMo(slowmo);
        getClients().sendUDP(new SlowmoMsg(slowmo));
    }

    public void checkIfIsEmpty()
    {
        if (getClients().size != 0)
            return;

        if (!BrainOutServer.getInstance().isAutoShutdown())
            return;

        BrainOutServer.TriggerShutdown(() -> getClients().size == 0);
    }

    private void onActiveAdded(final ActiveData activeData,
       final ActiveData.ComponentWriter componentWriter)
    {
        getClients().foreach(client ->
        {
            if (client instanceof PlayerClient)
            {
                ActiveFilterComponentData f = activeData.getComponent(ActiveFilterComponentData.class);

                if (f != null)
                {
                    if (!f.filters(client.getId()))
                        return;
                }

                Map map = activeData.getMap();
                if (map != null)
                {
                    ActiveFilterComponentData filter =
                            map.getComponents().getComponent(ActiveFilterComponentData.class);

                    if (filter != null && !filter.filters(client.getId()))
                    {
                        return;
                    }
                }

                PlayerClient playerClient = (PlayerClient)client;

                if (playerClient.isMapDownloading())
                {
                    playerClient.addOutgoingTCPMessage(() ->
                    {
                        ServerTeamVisibilityComponentData sv = activeData.getComponent(ServerTeamVisibilityComponentData.class);
                        if (sv != null)
                        {
                            sv.processVisibilityClient(playerClient, false, null);
                        }
                        return new NewActiveDataMsg(activeData, componentWriter, client.getId());
                    });
                }
                else
                {
                    playerClient.sendTCP(new NewActiveDataMsg(activeData, componentWriter, client.getId()));
                }
            }
        });
    }

    private void onActiveUpdated(final ActiveData activeData,
         final ActiveData.ComponentWriter componentWriter)
    {
        getClients().foreach(client ->
        {
            if (client instanceof PlayerClient)
            {
                ActiveFilterComponentData f = activeData.getComponent(ActiveFilterComponentData.class);
                if (f != null)
                {
                    if (!f.filters(client.getId()))
                        return;
                }

                Map map = activeData.getMap();
                if (map != null)
                {
                    ActiveFilterComponentData filter =
                            map.getComponents().getComponent(ActiveFilterComponentData.class);

                    if (filter != null && !filter.filters(client.getId()))
                    {
                        return;
                    }
                }

                PlayerClient playerClient = ((PlayerClient) client);

                if (playerClient.isMapDownloading())
                {
                    final int clientId = client.getId();
                    playerClient.addOutgoingTCPMessage(() ->
                        new UpdatedActiveDataMsg(activeData, componentWriter, clientId));
                }
                else
                {
                    playerClient.sendTCP(new UpdatedActiveDataMsg(activeData, componentWriter, client.getId()));
                }
            }
        });
    }

    private void onComponentUpdated(ComponentUpdatedEvent ev)
    {
        Object msg = new UpdatedComponentMsg(ev.data, ev.component);

        final ActiveData activeData = ev.data;
        final Component cmp = ev.component;

        getClients().foreach(client ->
        {
            if (client instanceof PlayerClient)
            {
                if (ev.predicate != null)
                {
                    if (!ev.predicate.check(client.getId()))
                        return;
                }

                PlayerClient playerClient = ((PlayerClient) client);

                if (playerClient.isMapDownloading())
                {
                    playerClient.addOutgoingTCPMessage(() -> new UpdatedComponentMsg(activeData, cmp));
                }
                else
                {
                    playerClient.sendTCP(msg);
                }
            }
        });
    }

    private void onActiveRemoved(ActiveData activeData, boolean ragdoll)
    {
        Object msg = new DeleteActiveDataMsg(activeData, ragdoll);

        for (ObjectMap.Entry<Integer, Client> entry : getClients())
        {
            Client client = entry.value;

            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);

                Map map = activeData.getMap();
                if (map != null)
                {
                    ActiveFilterComponentData filter =
                            map.getComponents().getComponent(ActiveFilterComponentData.class);

                    if (filter != null && !filter.filters(client.getId()))
                    {
                        continue;
                    }
                }

                if (playerClient.isMapDownloading())
                {
                    playerClient.addOutgoingTCPMessage(() -> msg);
                }
                else
                {
                    playerClient.sendTCP(msg);
                }
            }
        }
    }

    private static ObjectSet<String> kek = new ObjectSet<>();

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case activeAction:
            {
                ActiveActionEvent actionEvent = ((ActiveActionEvent) event);

                switch (actionEvent.action)
                {
                    case added:
                    {
                        onActiveAdded(actionEvent.activeData, actionEvent.componentWriter);

                        break;
                    }
                    case removed:
                    {
                        onActiveRemoved(actionEvent.activeData, actionEvent.flag);

                        break;
                    }
                    case updated:
                    {
                        onActiveUpdated(actionEvent.activeData, actionEvent.componentWriter);

                        break;
                    }
                }

                break;
            }

            case activeChangeDimension:
            {
                ActiveChangeDimensionEvent e = ((ActiveChangeDimensionEvent) event);

                onActiveDimensionChanged(e.activeData, e.oldId, e.oldDimension, e.newDimension);

                break;
            }

            case blockHitConfirmation:
            {
                BlockHitConfirmationEvent ev = ((BlockHitConfirmationEvent) event);

                confirmBlockHit(ev);

                break;
            }

            case componentUpdated:
            {
                ComponentUpdatedEvent componentUpdatedEvent = ((ComponentUpdatedEvent) event);

                onComponentUpdated(componentUpdatedEvent);

                break;
            }

            case playStateUpdated:
            {
                playStateChanged();

                break;
            }

            case setBlock:
            {
                SetBlockEvent setBlockEvent = ((SetBlockEvent) event);

                Object msg;

                if (setBlockEvent.blockData == null)
                {
                    msg = new BlockDestroyMsg(setBlockEvent.x, setBlockEvent.y, setBlockEvent.layer,
                        setBlockEvent.dimension);
                }
                else
                {
                    String data = Data.ComponentSerializer.toJson(setBlockEvent.blockData,
                        Data.ComponentWriter.TRUE, -1);

                    msg = new BlockAddMsg(setBlockEvent.x, setBlockEvent.y, setBlockEvent.layer, data,
                        setBlockEvent.dimension);
                }

                getClients().foreach(client ->
                {
                    if (client instanceof PlayerClient)
                    {
                        PlayerClient playerClient = (PlayerClient)client;

                        if (playerClient.isMapDownloading())
                        {
                            playerClient.addOutgoingTCPMessage(() -> msg);
                        }
                        else
                        {
                            playerClient.sendTCP(msg);
                        }
                    }
                });

                break;
            }
        }
        return false;
    }

    private void onActiveDimensionChanged(ActiveData activeData, int oldId, String oldDimension, String newDimension)
    {
        ServerActiveChangeDimensionMsg msg = new ServerActiveChangeDimensionMsg(
            oldId, activeData.getId(), activeData.getX(), activeData.getY(), activeData.getAngle(),
            oldDimension, newDimension
        );

        getClients().foreach(client ->
        {
            if (client instanceof PlayerClient)
            {
                ActiveFilterComponentData f = activeData.getComponent(ActiveFilterComponentData.class);

                if (f != null)
                {
                    if (!f.filters(client.getId()))
                        return;
                }

                PlayerClient playerClient = (PlayerClient)client;

                if (playerClient.isMapDownloading())
                {
                    playerClient.addOutgoingTCPMessage(() -> msg);
                }
                else
                {
                    playerClient.sendTCP(msg);
                }
            }
        });
    }

    private void confirmBlockHit(BlockHitConfirmationEvent ev)
    {
        if (ev.sender == null)
            return;

        int ownerId = ev.sender.getOwnerId();

        Client client = getClients().get(ownerId);

        if (client == null)
            return;

        if (!(client instanceof PlayerClient))
            return;

        PlayerClient playerClient = ((PlayerClient) client);

        if (ev.block == null)
            return;

        playerClient.sendTCP(new HitConfirmMsg("block", ev.dimension,
            -1, ev.x + 0.5f, ev.y + 0.5f, ev.damage));
    }

    public Console getConsole()
    {
        return console;
    }

    public ServerReliableManager getReliableManager()
    {
        return reliableManager;
    }

    public void cancelSpawn(Client client)
    {
        ServerSettings ss = BrainOutServer.Settings;

        switch (ss.getRespawnKind())
        {
            case waves:
            case dynamicWaves:
            {
                for (Array<Client> wave : waves)
                {
                    wave.removeValue(client, true);
                }
            }
        }
    }

    public boolean isQueuedForSpawning(Client client)
    {
        for (Array<Client> wave : waves)
        {
            if (wave.indexOf(client, true) >= 0)
                return true;
        }

        return false;
    }

    public void respawn(Client client, boolean extraWave)
    {
        if (client.isSpectator())
        {
            client.doSpawn();
            return;
        }

        GameMode gameMode = getGameMode();

        if (gameMode == null)
            return;

        if (!gameMode.isGameActive())
        {
            client.doSpawn();
            return;
        }

        ServerSettings ss = BrainOutServer.Settings;

        if (((ServerRealization) gameMode.getRealization()).immediateRespawn())
        {
            client.doSpawn();
            return;
        }

        switch (ss.getRespawnKind())
        {
            case immedeately:
            {
                client.doSpawn();
                break;
            }
            case waves:
            case dynamicWaves:
            {
                int waves = 0;

                if (waveTimer <= ss.getRespawnWaveMinTime())
                {
                    waves += 1;
                }

                if (extraWave)
                {
                    waves += 1;
                }

                Array<Client> nextWave = nextWave(waves);
                nextWave.removeValue(client, true);
                nextWave.add(client);

                client.nextRespawnIn(getNextWaveTime() * waves + waveTimer);

                break;
            }
        }
    }

    private Array<Client> nextWave(int number)
    {
        if (number >= waves.size)
        {
            int add = 1 + number - waves.size;

            for (int i = 0; i < add; i++)
            {
                Array<Client> newWave = new Array<>();
                waves.addLast(newWave);
            }
        }

        return waves.get(number);
    }

    private void doRespawn()
    {
        if (waves.size == 0)
            return;

        Array<Client> wave = waves.removeFirst();

        for (Client client: wave)
        {
            client.doSpawn();

            // remove this client from other waves shall it appear
            for (Array<Client> other : waves)
            {
                other.removeValue(client, true);
            }
        }
    }

    public void checkSpawn(Spawnable spawnable)
    {
        for (Array<Client> wave : waves)
        {
            checkWave(wave, spawnable);
        }

        getClients().sendTCP(new SimpleMsg(SimpleMsg.Code.updateSpawn));
    }

    private void checkWave(Array<Client> wave, Spawnable spawnable)
    {
        Array<Client> toRemove = null;

        for (Client client: wave)
        {
            if (client.getSpawnAt() == spawnable && !client.isValidSpawn())
            {
                if (toRemove == null) toRemove = new Array<>();

                toRemove.add(client);
            }
        }

        if (toRemove != null)
        {
            for (Client client: toRemove)
            {
                wave.removeValue(client, true);
                if (client instanceof PlayerClient)
                {
                    ((PlayerClient) client).sendInvalidSpawn();
                }
            }
        }
    }

    public void onDeath(Client client)
    {
        deathsRate.onDeath();
    }

    void setCurrentTime()
    {
        time = System.currentTimeMillis();
    }

    public long getTimePassed()
    {
        return System.currentTimeMillis() - time;
    }

    public void setSpeed(float speed)
    {
        // F hyper speed
        speed = Math.min(1.0F, speed);
        for (ServerMap map : Map.All(ServerMap.class))
        {
            map.setSpeed(speed);
        }

        getClients().sendTCP(new SetGameSpeedMsg(speed));
    }

    public void installPlugin(Plugin plugin)
    {
        plugins.add(plugin);
    }

    public void started()
    {
        for (Plugin plugin : plugins)
        {
            plugin.init();
        }
    }

    public MapSource getMapSource()
    {
        return mapSource;
    }

    public void setMapSource(MapSource mapSource)
    {
        this.mapSource = mapSource;
    }

    public Array<Team> getTeams()
    {
        return teams;
    }

    public void sendTerminalMessage(PlayerRights rights, String text)
    {
        getClients().sendTCP(new ChatMsg("terminal", text, "terminal", Color.WHITE, -1),
            client -> client.getRights().ordinal() >= rights.ordinal());
    }

    public int getAverageLevel()
    {
        int count = 0;
        int sum = 0;

        for (ObjectMap.Entry<Integer, Client> entry : getClients())
        {
            if (entry.value instanceof PlayerClient)
            {
                sum += entry.value.getLevel(Constants.User.LEVEL, 1);
                count++;
            }
        }

        if (count == 0)
        {
            return 0;
        }

        return sum / count;
    }

    public boolean isShutdownRequired()
    {
        return shutdownRequired;
    }

    public void forceSave()
    {
        for (ObjectMap.Entry<Integer, Client> clientEntry : BrainOutServer.Controller.getClients())
        {
            Client client = clientEntry.value;

            if (!client.isInitialized())
                continue;

            client.store();
        }
    }

    public void applyInitRoomSettings()
    {
        if (getInitRoomSettings() == null)
            return;

        if (getInitRoomSettings().getDisableBuilding().isDefined() &&
            getInitRoomSettings().getDisableBuilding().getValue())
        {
            BrainOutServer.Controller.getGlobalDefines().put("drill", "disabled");
        }

        String presetId = getInitRoomSettings().getPreset();

        if (presetId != null)
        {
            Preset preset = BrainOutServer.Settings.getPreset(presetId);

            if (preset != null)
            {
                if (Log.INFO) Log.info("Preset has been used: " + presetId);

                BrainOutServer.Controller.getGlobalDefines().put("preset", presetId);

                BrainOutServer.Controller.getGlobalDefines().putAll(preset.defines);
            }
        }
    }

    public Preset getCurrentPreset()
    {
        if (getInitRoomSettings() == null)
            return null;

        String presetId = getInitRoomSettings().getPreset();

        if (presetId == null || presetId.isEmpty())
            return null;

        return BrainOutServer.Settings.getPreset(presetId);
    }

    public void checkDeployment()
    {
        GameMode gameMode = getGameMode();

        if (gameMode != null)
        {
            ServerRealization realization = ((ServerRealization) gameMode.getRealization());

            if (realization != null && !realization.needsDeploymentsCheck())
            {
                return;
            }
        }

        if (BrainOutServer.getInstance().hasOnlineController())
        {
            BrainOutServer.getInstance().checkOnlineControllerDeployment();
        }
    }

    public void updateRoomSettings()
    {
        RoomSettings roomSettings = getRoomSettings();

        GameService.RoomSettings stt = new GameService.RoomSettings();
        roomSettings.write(stt);

        if (BrainOutServer.Settings.getZone() != null)
        {
            stt.add("zone", BrainOutServer.Settings.getZone());

            int a = 0;
            int b = 0;
            for (Client client : BrainOutServer.Controller.getClients().values())
            {
                if (!(client instanceof PlayerClient))
                    continue;
                PlayerClient playerClient = ((PlayerClient) client);
                GlobalConflict.Owner owner = GlobalConflict.GetAccountOwner(
                    playerClient.getAccount(), playerClient.getClanId(), BrainOutServer.Settings.getLastConflict()
                );
                if (owner == GlobalConflict.Owner.a)
                {
                    a++;
                }
                if (owner == GlobalConflict.Owner.b)
                {
                    b++;
                }
            }

            if (a > 0)
            {
                stt.add("players-a", a);
            }

            if (b > 0)
            {
                stt.add("players-b", b);
            }
        }

        if (Log.INFO) Log.info("Updating settings: " + stt.toString());

        if (BrainOutServer.getInstance().hasOnlineController())
        {
            BrainOutServer.getInstance().updateOnlineControllerSettings(stt);
        }
    }

    public void setInitRoomSettings(RoomSettings initRoomSettings)
    {
        this.initRoomSettings = initRoomSettings;
    }

    public RoomSettings getInitRoomSettings()
    {
        return initRoomSettings;
    }

    public boolean isLobby()
    {
        PlayState ps = getPlayState();

        return ps instanceof ServerPSGame && ((ServerPSGame) ps).getMode() != null &&
            ((ServerPSGame) ps).getMode().getID() == GameMode.ID.lobby;
    }

    public boolean isDropEnabled(PlayerData playerData)
    {
        PlayState ps = getPlayState();

        if (!(ps instanceof ServerPSGame))
            return false;

        GameMode mode = ((ServerPSGame) ps).getMode();

        if (mode == null)
            return false;

        return ((ServerRealization) mode.getRealization()).isDeathDropEnabled(playerData);
    }

    public boolean isFreePlay()
    {
        PlayState ps = getPlayState();

        return ps instanceof ServerPSGame && ((ServerPSGame) ps).getMode() != null &&
            ((ServerPSGame) ps).getMode().getID() == GameMode.ID.free;
    }

    public Team getClanWarTeam(String clan)
    {
        return clanTeams.get(clan);
    }

    public ObjectMap<String, Team> getClanTeams()
    {
        return clanTeams;
    }

    public ClanPartyMember getClanPartyMember(String accountId)
    {
        if (clanPartyMembers == null)
            return null;

        return clanPartyMembers.get(accountId);
    }

    public void setPartyMembers(JSONObject jsonObject)
    {
        if (BrainOutServer.getInstance().isClanWar())
        {
            clanPartyMembers = new ObjectMap<>();

            for (String account : jsonObject.keySet())
            {
                JSONObject data = jsonObject.optJSONObject(account);

                if (data != null)
                {
                    clanPartyMembers.put(account, new ClanPartyMember(data));
                }
            }
        }
    }

    public void setPartySettings(JSONObject settings)
    {
        if (BrainOutServer.getInstance().isClanWar())
        {
            if (Log.INFO) Log.info("Clan war with " + teams.size + " teams.");

            String clanA = settings.optString("clan-a");
            String clanB = settings.optString("clan-b");

            if (clanA != null)
            {
                if (Log.INFO) Log.info("Supplied clan-a: " + clanA);
            }

            if (clanB != null)
            {
                if (Log.INFO) Log.info("Supplied clan-b: " + clanB);
            }

            Team teamA = null, teamB = null;

            if (clanA != null && clanB != null)
            {
                for (Team team : teams)
                {
                    if (team instanceof SpectatorTeam)
                        break;

                    if (teamA == null)
                    {
                        teamA = team;
                        continue;
                    }

                    teamB = team;
                    break;
                }

                if (teamA != null && teamB != null)
                {
                    if (Log.INFO) Log.info("Defined clan teams as: " + clanA + " are " + teamA +
                        " and " + clanB + " are " + teamB + ".");

                    clanTeams.put(clanA, teamA);
                    clanTeams.put(clanB, teamB);
                }
            }
        }
    }

    @Override
    public long getCurrentTime()
    {
        return System.currentTimeMillis() / 1000L;
    }

    @Override
    public boolean isServer()
    {
        return true;
    }

    @Override
    public boolean isOnlineEnabled()
    {
        return !BrainOutServer.getInstance().offline;
    }
}
