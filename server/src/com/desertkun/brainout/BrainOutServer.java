package com.desertkun.brainout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.mode.*;
import com.desertkun.brainout.online.Reward;
import com.desertkun.brainout.online.RoomSettings;
import com.desertkun.brainout.online.ServerReward;
import com.desertkun.brainout.playstate.*;
import com.desertkun.brainout.server.*;
import com.desertkun.brainout.server.http.ContentHttpServer;
import com.desertkun.brainout.server.mapsource.*;
import com.desertkun.brainout.utils.ExceptionHandler;
import com.desertkun.brainout.utils.NullRenderAttachmentLoader;
import com.desertkun.brainout.utils.RoomIDEncryption;
import com.desertkun.brainout.utils.SteamAPIUtil;
import org.anthillplatform.runtime.AnthillRuntime;
import org.anthillplatform.runtime.requests.Request;
import org.anthillplatform.runtime.server.GameServerController;
import org.anthillplatform.runtime.services.DiscoveryService;
import org.anthillplatform.runtime.services.GameService;
import org.anthillplatform.runtime.services.LoginService;
import com.esotericsoftware.kryonet.KryoSerialization;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.spine.SkeletonJson;
import org.anthillplatform.runtime.services.ProfileService;
import org.anthillplatform.runtime.util.ApplicationInfo;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.desertkun.brainout.Version.ENV_SERVICE;

@SuppressWarnings("PointlessBooleanExpression")
public class BrainOutServer extends BrainOut implements Runnable
{
    private Listener serverListener;
    private Server server;
    private boolean working, packagesLoaded;

    private long previousTime;
    private ContentHttpServer httpServer;

    public static ServerEnvironment                 Env;
    public static GameServerController              OnlineController;
    public static ServerController                  Controller;
    public static ServerSettings                    Settings;
    public static AnthillRuntime                    Online;

    static
    {
        IOReactorConfig.DEFAULT.setIoThreadCount(2);
    }

    private final static Queue<Runnable> runnables = new ArrayDeque<>();
    private final static Queue<Runnable> step = new ArrayDeque<>();
    private static int max_queue = 0;
    public final static int MAX_QUEUE_SIZE = 256;

    @Parameter
    private List<String> otherArguments = new ArrayList<>();

    @Parameter(names = {"--settings", "-s"}, description = "Server Settings file location")
    private String serverSettings = "server-settings.json";

    @Parameter(names = {"--map", "--maps", "-m"}, description = "Map or map set set file location")
    private String map = null;

    @Parameter(names = {"--mode"}, description = "Mode")
    private GameMode.ID mode = GameMode.ID.normal;

    @Parameter(names = {"--autoshutdown", "-au"}, arity = 1)
    private boolean autoShutdown;

    @Parameter(names = {"--clan-war", "-cw"}, arity = 1)
    private boolean clanWar;

    @Parameter(names = {"--offline"}, description = "Offline Mode")
    public boolean offline = false;

    @Parameter(names = {"--auto-start", "-as"}, arity = 1)
    private boolean autoStart = true;

    @Parameter(names = {"--debug-window", "-dw"}, arity = 0)
    private boolean debugWindow = false;

    private int ensureBots = 0;
    private String autoExec = null;

    @Parameter(names = {"--custom", "-cs"}, arity = 0)
    private boolean custom = false;

    private int tcp = ServerConstants.Connection.DEFAULT_TCP_PORT;
    private int udp = ServerConstants.Connection.DEFAULT_UDP_PORT;
    private int http = ServerConstants.Connection.DEFAULT_HTTP_PORT;

    public BrainOutServer(String[] args, ServerEnvironment env)
    {
        super(env);

        Env = env;

        new JCommander(this, args);

        Controller = new ServerController(this);

        ApplicationInfo applicationInfo = new ApplicationInfo(
            ServerConstants.Name.GAMESPACE,
            ServerConstants.Name.APP_NAME, Version.VERSION);

        Online = AnthillRuntime.Create(ENV_SERVICE, applicationInfo);

        String sockets = null;

        if (otherArguments.size() >= 1)
        {
            sockets = otherArguments.get(0);

            if (otherArguments.size() >= 2)
            {
                String[] ports = otherArguments.get(1).split(",");

                try
                {
                    switch (ports.length)
                    {
                        case 1:
                        {
                            tcp = Integer.valueOf(ports[0]);
                            udp = -1;
                            http = -1;
                            break;
                        }
                        case 2:
                        {
                            tcp = Integer.valueOf(ports[0]);
                            udp = Integer.valueOf(ports[1]);
                            http = -1;
                            break;
                        }
                        case 3:
                        default:
                        {
                            tcp = Integer.valueOf(ports[0]);
                            udp = Integer.valueOf(ports[1]);
                            http = Integer.valueOf(ports[2]);
                            break;
                        }
                    }
                }
                catch (NumberFormatException e)
                {
                    if (Log.ERROR) Log.error("Error while parsing ports: " + e.getMessage());
                }
            }
        }

        if ((!offline) && sockets != null && !sockets.equals("no"))
        {
            if (sockets.startsWith("/"))
            {
                sockets = "ipc://" + sockets;
            }

            createOnlineController(sockets);
        }

        serverListener = null;
        server = null;
        working = false;
        packagesLoaded = false;
    }

    protected void createOnlineController(String sockets)
    {
        OnlineController = new BrainOutServerController(sockets);
    }

    @Override
    public Controller getController()
    {
        return BrainOutServer.Controller;
    }

    public static BrainOutServer getInstance()
    {
        return (BrainOutServer)instance;
    }

    public static BrainOut initServerInstance(String[] args, ServerEnvironment env)
    {
        instance = new BrainOutServer(args, env);
        return getInstance();
    }

    public void packagesLoaded()
    {
        if (Log.INFO) Log.info("Packages loaded!");

        packagesLoaded = true;
        working = true;

        if (Log.INFO) Log.info("Server started");
    }

    public boolean isAutoShutdown()
    {
        return autoShutdown;
    }

    public boolean isClanWar()
    {
        return clanWar;
    }

    public int getEnsureBots()
    {
        return ensureBots;
    }

    public String getAutoExec()
    {
        return autoExec;
    }

    public void initGdx()
    {
        Gdx.files = new Lwjgl3Files();
        Gdx.net = new Lwjgl3Net(new Lwjgl3ApplicationConfiguration());
    }

    public boolean start()
    {
        initGdx();

        if (Log.INFO) Log.info("BrainOut server " + Version.VERSION);

        init();

        if (serverSettings.isEmpty())
        {
            if (Log.ERROR) Log.error("Server Settings is not defined!");
            return false;
        }

        FileHandle settingsFile = Gdx.files.local(serverSettings);

        if (!settingsFile.exists())
        {
            if (Log.ERROR) Log.error("Server Settings file was not found!");
            return false;
        }

        Json json = new Json();
        BrainOut.R.tag(json);
        Settings = json.fromJson(ServerSettings.class, settingsFile);

        if (Log.INFO) Log.info("Settings parsed!");

        Controller.getClients().init();

        String roomSettingsData = BrainOutServer.getenv("room_settings", "room:settings");

        if (roomSettingsData != null)
        {
            JSONObject roomJson;

            try
            {
                roomJson = new JSONObject(roomSettingsData);
            }
            catch (JSONException e)
            {
                if (Log.ERROR) Log.error("Failed to load room Settings!");
                return false;
            }

            if (Log.INFO) Log.info("Applying room settings: " + roomJson.toString());

            RoomSettings settings = new RoomSettings();
            settings.read(roomJson);

            if (settings.getZone() != null)
            {
                BrainOutServer.Settings.setZone(settings.getZone());
                if (Log.INFO) Log.info("Global Conflict Mode: " + settings.getZone());
            }

            setRoomSettings(settings);
        }
        else
        {
            if (custom)
            {
                if (Log.ERROR) Log.error("No room settings on custom mode!");
                return false;
            }
        }

        if (map != null && !map.isEmpty())
        {
            String additionalFile = map;

            if (additionalFile.endsWith(".shuffle"))
            {
                ShuffledMapSource source = new ShuffledMapSource(additionalFile, mode);

                BrainOutServer.Controller.setMapSource(source);
            }
            else if (additionalFile.endsWith(".json"))
            {
                MapSource source = BrainOut.R.JSON.fromJson(MapSource.class,
                        new FileHandle(additionalFile));

                BrainOutServer.Controller.setMapSource(source);
            }
            else
            {
                SingleMapSource source = new SingleMapSource(additionalFile, mode);

                BrainOutServer.Controller.setMapSource(source);
            }
        }
        else
        {
            switch (mode)
            {
                case lobby:
                {
                    BrainOutServer.Controller.setMapSource(new SingleMapSource("maps/lobby.map", mode));
                    break;
                }
                case duel:
                {
                    BrainOutServer.Controller.setMapSource(new SingleMapSource("maps/duel.map", mode));
                    break;
                }
                case editor:
                case editor2:
                {
                    BrainOutServer.Controller.setMapSource(new EmptyMapSource(mode));
                    break;
                }
                default:
                {
                    if (!custom)
                    {
                        if (Log.ERROR) Log.error("Neither map or mode is correctly defined.");
                        System.exit(1);
                        break;
                    }
                }
            }
        }

        if (Log.INFO) Log.info("Map source loaded!");

        if (Settings == null)
        {
            if (Log.ERROR) Log.error("Failed to load server Settings!");
            return false;
        }

        serverListener = new Listener.ThreadedListener(new ServerListener(Controller));

        Log.ERROR = true;

        server = new Server(323840, 80480, new KryoSerialization(Kryo));

        initCommonData(Kryo);

        if (BrainOutServer.Settings.getZone() != null)
        {
            BrainOutServer.PackageMgr.setDefine("zone", BrainOutServer.Settings.getZone());
        }

        if (Log.INFO) Log.info("Kryo inited.");

        Controller.initSettings(Settings);

        if (Log.INFO) Log.info("Controller settings inited.");

        server.addListener(serverListener);
        server.start();

        try
        {
            if (udp != -1)
            {
                server.bind(tcp, udp);
            }
            else
            {
                server.bind(tcp);
            }
        }
        catch (IOException e)
        {
            if (Log.ERROR) Log.error("Failed to bind server: " + e.getMessage());
            e.printStackTrace();

            return false;
        }

        if (http != -1)
        {
            try
            {
                httpServer = new ContentHttpServer(http);
            }
            catch (IOException e)
            {
                if (Log.ERROR) Log.error("Failed to open http server: " + e.getMessage());
                e.printStackTrace();

                return false;
            }

            httpServer.start();

            if (Log.INFO) Log.info("Http server picked up at port " + http);
        }

        Controller.next(this::loaded);

        long timeMillis = System.currentTimeMillis();

        previousTime = 0;

        while (!packagesLoaded)
        {
            if (System.currentTimeMillis() >= timeMillis)
            {
                if (Log.INFO) Log.info("Loaded " + (PackageMgr.getLoadProgress() * 100) + "%");
                timeMillis = System.currentTimeMillis() + 1000;
            }

            try
            {
                updateStep();
            }
            catch (Exception e)
            {
                ExceptionHandler.handle(e);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            if (Log.INFO) Log.info("Caught shutdown signal, forcing profiles to save");

            BrainOutServer.PostRunnable(BrainOutServer::TriggerShutdown);

            try
            {
                Thread.sleep(15000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            if (Log.INFO) Log.info("Exiting!");
            Runtime.getRuntime().halt(0);
        }));

        return true;
    }

    private void loaded(boolean success)
    {
        if (!success)
        {
            if (Log.ERROR) Log.error("Failed to load.");
            BrainOut.exit(1);
        }

        packagesLoaded();

        onlineLibInitialized();

        try
        {
            Controller.init();
        }
        catch (Exception e)
        {
            ExceptionHandler.handle(e);

            if (Log.ERROR) Log.error("Failed to init controller: " + e.getMessage());
        }

        if (BrainOutServer.Settings.getZone() != null)
        {
            LoginService loginService = LoginService.Get();
            ProfileService profileService = ProfileService.Get();

            if (loginService != null && profileService != null)
            {
                profileService.getMyProfile(loginService.getCurrentAccessToken(),
                        (profileService1, request, result, profile) -> BrainOutServer.PostRunnable(() ->
                {
                    if (result == Request.Result.success)
                    {
                        JSONObject conflict = profile.optJSONObject("conflict");
                        if (conflict == null)
                            return;
                        BrainOutServer.Settings.setLastConflict(conflict.optLong("last"));
                    }
                }));
            }
        }

        if (Log.INFO) Log.info("Server successfully picked up!");

        pickedUp();
    }

    public static String getenv(String key, String or)
    {
        String value = System.getenv(key);

        if (value == null)
        {
            return System.getenv(or);
        }

        return value;
    }

    private void setCustomSettings(RoomSettings settings)
    {
        if (!settings.getMap().isDefined())
        {
            if (Log.ERROR) Log.error("No map ID defined in custom mode");
            BrainOut.exit(-1);
            return;
        }

        String workshopId = settings.getMap().getValue();

        try
        {
            Long.valueOf(workshopId);
        }
        catch (NumberFormatException ignored)
        {
            if (Log.ERROR) Log.error("Bad custom map id");
            BrainOut.exit(-1);
            return;
        }

        SteamAPIUtil.WorkshopItemResult workshopResult = SteamAPIUtil.GetWorkshopItem(workshopId);
        if (workshopResult == null)
        {
            if (Log.ERROR) Log.error("Failed to acquire workshop item info.");
            BrainOut.exit(-1);
            return;
        }

        InputStream inputStream = SteamAPIUtil.DownloadWorkshopMap(workshopId, workshopResult.getTimeUpdated());

        if (inputStream == null)
        {
            if (Log.ERROR) Log.error("Failed to download custom map.");
            BrainOut.exit(-1);
            return;
        }


        if (Log.INFO) Log.info("Map subscribers: " + workshopResult.getSubscribers());

        settings.setSubscribers(workshopResult.getSubscribers());

        CustomMapSource source;

        try
        {
            source = new CustomMapSource(inputStream, workshopId, workshopResult);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            BrainOut.exit(-1);
            return;
        }

        if (settings.getMode().isDefined())
        {
            source.setPreferableMode(settings.getMode().getValue());
        }

        BrainOutServer.Controller.setMapSource(source);
    }

    public static boolean IsCustom()
    {
        return getInstance().custom;
    }

    private void setRoomSettings(RoomSettings settings)
    {
        BrainOutServer.Controller.setInitRoomSettings(settings);
        BrainOutServer.Controller.applyInitRoomSettings();

        if (custom)
        {
            setCustomSettings(settings);
            return;
        }

        MapSource mapSource = BrainOutServer.Controller.getMapSource();

        String map = null;
        String mode = null;

        if (settings.getMap().isDefined())
        {
            map = settings.getMap().getValue();
        }

        if (settings.getMode().isDefined())
        {
            mode = settings.getMode().getValue();

            if (settings.getKeepMode().isDefined() && settings.getKeepMode().getValue())
            {
                mapSource.setForceMode(mode);
            }
        }

        if (mode != null || map != null)
        {
            mapSource.insert(map, mode);
        }

    }

    private RoomSettings getRoomSettings()
    {
        return BrainOutServer.Controller.getRoomSettings();
    }

    private void onlineLibInitialized()
    {
        String accessToken = BrainOutServer.getenv("login_access_token", "login:access_token");
        String maxPlayers = BrainOutServer.getenv("game_max_players", "game:max_players");
        String partySettings = BrainOutServer.getenv("party_settings", "party:settings");
        String partyMembers = BrainOutServer.getenv("party_members", "party:members");
        String serverSettings = BrainOutServer.getenv("server_settings", "server:settings");
        String roomId = BrainOutServer.getenv("room_id", "room:id");

        if (serverSettings != null)
        {
            JSONObject serverSettingsJson = new JSONObject(serverSettings);
            ensureBots = serverSettingsJson.optInt("bots", ensureBots);
            if (Log.INFO) Log.info("Ensure bots: " + ensureBots);

            autoExec = serverSettingsJson.optString("exec");
            if (autoExec != null)
            {
                if (Log.INFO) Log.info("Auto exec: " + autoExec);
            }
        }

        if (roomId != null)
        {
            String encode = RoomIDEncryption.EncryptHumanReadable(roomId);
            if (Log.INFO) Log.info("Encoded Room Id: " + encode);
        }

        if (maxPlayers != null)
        {
            try
            {
                Controller.getClients().setMaxPlayers(Integer.valueOf(maxPlayers));
            }
            catch (NumberFormatException ignored) {}
        }

        if (partySettings != null)
        {
            Controller.setPartySettings(new JSONObject(partySettings));
        }

        if (partyMembers != null)
        {
            Controller.setPartyMembers(new JSONObject(partyMembers));
        }

        if (BrainOut.OnlineEnabled())
        {
            if (accessToken == null)
            {
                if (Log.ERROR) Log.error("No access_token environment variable!");
                BrainOut.exit(-1);
            }

            String services = BrainOutServer.getenv("discovery_services", "discovery:services");

            if (services == null)
            {
                if (Log.ERROR) Log.error("No services environment variable!");
                BrainOut.exit(-1);
            }

            JSONObject servicesList = new JSONObject(services);

            Online.setService(DiscoveryService.ID, "");

            for (String id : servicesList.keySet())
            {
                String location = servicesList.getString(id);
                Online.setService(id, location);
            }

            LoginService loginService = LoginService.Get();

            if (loginService == null)
            {
                if (Log.ERROR) Log.error("No login service!");
                BrainOut.exit(-1);
                return;
            }

            loginService.setCurrentAccessToken(loginService.newAccessToken(accessToken));
        }

        if (Log.INFO) Log.info("Initialized!");

        GameService.RoomSettings settings = new GameService.RoomSettings();

        RoomSettings roomSettings = getRoomSettings();

        if (roomSettings != null)
        {
            roomSettings.write(settings);

            if (Log.INFO) Log.info("Inited with settings: " + settings.toString());
        }
        else
        {
            if (Log.WARN) Log.warn("No room settings!");
        }

        if (hasOnlineController())
        {
            initOnlineController(settings);
        }
    }

    public boolean hasOnlineController()
    {
        return OnlineController != null;
    }

    protected void initOnlineController(GameService.RoomSettings settings)
    {
        OnlineController.inited(settings, success ->
        {
            if (!success)
            {
                BrainOut.exit(-1);
            }
        });
    }

    private void pickedUp()
    {
        Map.SetMainThread(Thread.currentThread());

        Controller.started();

        if (Log.INFO) Log.info("Picked up");

        Thread inputThread = new Thread(new ServerInput());
        inputThread.start();
    }

    @Override
    public boolean hasTag(String tag)
    {
        return "server".equals(tag);
    }

    @Override
    public void run()
    {
        while (working)
        {
            try
            {
                updateStep();
            }
            catch (Exception e)
            {
                ExceptionHandler.handle(e);
            }
        }
    }

    public void stop()
    {
        if (httpServer != null)
        {
            httpServer.stop();
        }
        if (server != null)
        {
            server.stop();
        }
    }

    public void startMain()
    {
        if (start())
        {
            if (debugWindow)
            {
                new ServerDebugWindow();
            }

            run();
        }

        stop();
    }

    public static int GetMaxQueueSize()
    {
        return max_queue;
    }

    private void updateStep()
    {
        long time = System.nanoTime() / 1000000L;
        float dt = MathUtils.clamp((time - previousTime) / 1000.f, 0.0f, 0.25f);

        previousTime = time;

        {
            long before = System.currentTimeMillis();
            update(dt);
            long after = System.currentTimeMillis();
            if (after > before + 200)
            {
                long took = after - before;
                if (Log.INFO) Log.info("Update took: " + (took));
            }
        }

        try {
            Thread.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        updateOnlineController();

        step.clear();
        synchronized (runnables)
        {
            step.addAll(runnables);
            runnables.clear();
        }

        if (step.size() > max_queue + 20)
        {
            max_queue = step.size();

            if (Log.INFO)
            {
                Log.info("New maximum");
                ObjectMap<String, Integer> cases = new ObjectMap<>();
                for (Runnable runnable : step)
                {
                    cases.put(runnable.getClass().getName(), cases.get(runnable.getClass().getName(), 0) + 1);
                }
                Array<String> sorted = cases.keys().toArray();
                sorted.sort((o1, o2) -> cases.get(o2) - cases.get(o1));
                for (int i = 0, t = Math.min(sorted.size, 32); i < t; i++)
                {
                    String className = sorted.get(i);
                    Log.info("Class " + className + " has " + cases.get(className) + " uses");
                }
            }
        }

        long runnablesBefore = System.currentTimeMillis();

        while (!step.isEmpty())
        {
            try
            {

                Runnable r = step.remove();
                long before = System.currentTimeMillis();
                r.run();
                long after = System.currentTimeMillis();
                if (after - before > 500)
                {
                    long took = after - before;
                    if (Log.INFO) Log.info("Runnable " + r.getClass().getName() + " took: " + (took));
                }
            }
            catch (Exception e)
            {
                ExceptionHandler.reportCrash(e, "crashreport", () -> {});
            }
        }

        long runnablesAfter = System.currentTimeMillis();
        if (runnablesAfter - runnablesBefore > 500)
        {
            long took = runnablesAfter - runnablesBefore;
            if (Log.INFO) Log.info("Runnables took: " + (took));
        }
    }

    protected void updateOnlineController()
    {
        if (OnlineController != null)
        {
            OnlineController.update();
        }
    }

    private void update(float dt)
    {
        Operations.update(dt);
        Controller.update(dt);
        EventMgr.update();
    }

    public ServerSettings getSettings()
    {
        return Settings;
    }

    public static void PostRunnable(Runnable runnable, int maxSize)
    {
        synchronized (runnables)
        {
            if (runnables.size() > maxSize)
                return;

            runnables.add(runnable);
        }
    }

    public static void PostRunnable(Runnable runnable)
    {
        synchronized (runnables)
        {
            runnables.add(runnable);
        }
    }

    @Override
    public GameMode newMode(GameMode.ID id)
    {
        switch (id)
        {
            case editor:
            {
                return new GameModeEditor(ServerEditorRealization.class);
            }
            case editor2:
            {
                return new GameModeEditor2(ServerEditor2Realization.class);
            }
            case lobby:
            {
                return new GameModeLobby(ServerLobbyRealization.class);
            }
            case normal:
            {
                return new GameModeNormal(ServerNormalRealization.class);
            }
            case duel:
            {
                return new GameModeDuel(ServerDuelRealization.class);
            }
            case assault:
            {
                return new GameModeAssault(ServerAssaultRealization.class);
            }
            case foxhunt:
            {
                return new GameModeFoxHunt(ServerFoxHuntRealization.class);
            }
            case gungame:
            {
                return new GameModeGunGame(ServerGunGameRealization.class);
            }
            case zombie:
            {
                return new GameModeZombie(ServerZombieRealization.class);
            }
            case domination:
            {
                return new GameModeDomination(ServerDominationRealization.class);
            }
            case deathmatch:
            {
                return new GameModeDeathmatch(ServerDeathmatchRealization.class);
            }
            case ctf:
            {
                return new GameModeCTF(ServerCTFRealization.class);
            }
            case free:
            {
                return new GameModeFree(ServerFreeRealization.class);
            }
        }

        return null;
    }

    @Override
    public Reward newReward()
    {
        return new ServerReward();
    }

    @Override
    public PlayState newPlayState(PlayState.ID id)
    {
        switch (id)
        {
            case game:
            {
                return new ServerPSGame();
            }
            case endgame:
            {
                return new ServerPSEndGame();
            }
            case empty:
            {
                return new ServerPSEmpty();
            }
        }

        return null;
    }

    public void checkOnlineControllerDeployment()
    {
        OnlineController.checkDeployment(
        success ->
        {
            if (!success)
            {
                BrainOutServer.PostRunnable(BrainOutServer::TriggerShutdown);
            }
        });
    }

    public void updateOnlineControllerSettings(GameService.RoomSettings stt)
    {
        OnlineController.updateSettings(stt, success ->
        {
            if (success)
            {
                if (Log.INFO) Log.info("Settings updated!");
            }
            else
            {
                if (Log.ERROR)
                    Log.error("Failed to update settings!");
            }
        });
    }


    public interface OnlineControllerPlayerJoinedCallback
    {
        void result(boolean success, LoginService.AccessToken token,
                    String account, String credential,
                    LoginService.Scopes scopes, JSONObject info);
    }

    public interface OnlineControllerPlayerLeftCallback
    {
        void result(boolean success);
    }

    public void onlineControllerJoined(String key, LoginService.AccessToken accessToken,
                                       String extendScopes, OnlineControllerPlayerJoinedCallback callback)
    {
        OnlineController.joined(key, accessToken, extendScopes, callback::result);
    }

    public void onlineControllerLeft(String key, OnlineControllerPlayerLeftCallback callback)
    {
        OnlineController.left(key, callback::result);
    }

    public interface ShutdownPredicate
    {
        boolean shutdown();
    }

    public static void TriggerShutdown()
    {
        TriggerShutdown(null);
    }

    private static boolean ShuttingDown;
    public static boolean IsShuttingDown()
    {
        return ShuttingDown;
    }

    public static void TriggerShutdown(ShutdownPredicate predicate)
    {
        if (Log.INFO) Log.info("Shutting down in 5 seconds.");

        ShuttingDown = true;

        GameMode gameMode = Controller.getGameMode();
        if (gameMode != null)
        {
            ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());
            serverRealization.onShuttingDown();
        }

        Controller.forceSave();

        for (ObjectMap.Entry<Integer, Client> entry : Controller.getClients())
        {
            if (entry.value instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) entry.value);

                playerClient.sendChat("{MP_PLAYER_SHUTTING_DOWN}");
            }
        }

        BrainOut.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                if (predicate == null || predicate.shutdown())
                {
                    BrainOutServer.PostRunnable(BrainOutServer::DoShutdown);
                }
                else
                {
                    if (Log.INFO) Log.info("Shutdown cancelled.");
                }
            }
        }, 5000);
    }

    private static void DoShutdown()
    {
        for (ObjectMap.Entry<Integer, Client> entry : Controller.getClients())
        {
            entry.value.disconnect(DisconnectReason.shutdown, "Shutdown");
        }

        BrainOut.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.getInstance().exit();
            }
        }, 1000);
    }

    private void exit()
    {
        if (Log.INFO) Log.info("Exiting.");

        BrainOut.exit(-1);
    }

    public String validateText(String text)
    {
        return text;
    }

    @Override
    public void postRunnable(Runnable runnable)
    {
        BrainOutServer.PostRunnable(runnable);
    }

    @Override
    protected SkeletonJson newSkeleton()
    {
        return new SkeletonJson(new NullRenderAttachmentLoader());
    }

    public boolean isAutoStart()
    {
        return autoStart;
    }
}
