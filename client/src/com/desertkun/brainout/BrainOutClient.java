package com.desertkun.brainout;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.desertkun.brainout.client.ClientController;
import com.desertkun.brainout.client.ClientListener;
import com.desertkun.brainout.client.SocialController;
import com.desertkun.brainout.client.settings.ClientSettings;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.gs.GameState;
import com.desertkun.brainout.gs.MainMenuState;
import com.desertkun.brainout.managers.*;
import com.desertkun.brainout.menu.tutorial.Tutorials;
import com.desertkun.brainout.menu.ui.ActionList;
import com.desertkun.brainout.menu.ui.Avatars;
import com.desertkun.brainout.mode.*;
import com.desertkun.brainout.online.ClientReward;
import com.desertkun.brainout.online.Reward;
import com.desertkun.brainout.online.VoiceChatManager;
import com.desertkun.brainout.packages.*;
import com.desertkun.brainout.playstate.*;
import com.desertkun.brainout.utils.*;
import com.esotericsoftware.minlog.Log;
import org.anthillplatform.runtime.AnthillRuntime;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import org.anthillplatform.runtime.util.ApplicationInfo;

import java.io.*;
import java.nio.IntBuffer;
import java.util.Stack;

import static com.desertkun.brainout.Version.ENV_SERVICE;

public class BrainOutClient extends BrainOut implements ApplicationListener
{
    public static ShapeRenderer 				ShapeRenderer;
    public static SkinFromStream				Skin;
    public static FontManager					FontMgr;
    public static TextureManager				TextureMgr;
    public static ClientEnvironment             Env;

    protected static Stack<GameState>           statesStack;

    public static ClientController              ClientController;
    public static SocialController              SocialController;

    public static int							screenWidth;
    public static int							screenHeight;
    public static MusicManager                  MusicMng;
    public static ClientSettings                ClientSett;
    public static AnthillRuntime                Online;
    public static ActionList                    Actions;
    //public static Analytics                     Analytics;
    public static GamePadManager                GamePadMgr;
    public static VoiceChatManager              Voice;

    @Parameter(names = {"--connect"}, description = "Direct connect location")
    public static String ConnectToLocation = null;

    @Parameter(names = {"--join-room"}, description = "ID of the room to connect to")
    public static String ConnectRoomId = null;

    @Parameter(names = {"--free-play-join"}, description = "ID of the freeplay party to connect to")
    public static String ConnectFreePlayPartyId = null;

    @Parameter(names = {"--unsafe", "-us"}, description = "Accept unsigned packages")
    public boolean unsafe = false;

    @Parameter(names = {"--offline"}, description = "Offline Mode")
    public boolean offline = false;

    public static TimeMeasure _render = new TimeMeasure();
    public static TimeMeasure _update = new TimeMeasure();
    public static TimeMeasure _events = new TimeMeasure();
    public static TimeMeasure _controller = new TimeMeasure();

    public static BrainOutClient initClientInstance(ClientEnvironment env, ClientSettings clientSettings)
    {
        instance = new BrainOutClient(env, clientSettings);
        return (BrainOutClient)instance;
    }

    public static BrainOutClient getInstance() {
        return (BrainOutClient)instance;
    }

    public BrainOutClient(ClientEnvironment env, ClientSettings clientSettings)
    {
        super (env);

        JCommander commander = new JCommander(this);
        commander.setAcceptUnknownOptions(true);
        commander.parse(env.getArgs());

        Env = env;
        ClientSett = clientSettings;
    }

    @Override
    public void create()
    {
        statesStack = new Stack<>();

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        ApplicationInfo applicationInfo = new ApplicationInfo(
            ClientConstants.Name.GAMESPACE,
            ClientConstants.Name.APP_NAME,
            Version.VERSION);

        Online = AnthillRuntime.Create(ENV_SERVICE, applicationInfo);

        Skin = new SkinFromStream();
        ShapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();

        initData();

        TextureMgr = new TextureManager();
        FontMgr = new FontManager();
        Actions = new ActionList();

        ClientController = new ClientController(this, ClientSett);
        SocialController = new SocialController();
        Network = Env.newNetworkClient(Kryo, new ClientListener(ClientController));
        Voice = new VoiceChatManager();

        initCommonData(Kryo);

        MusicMng = new MusicManager();
        Tutorials.Init();

        if (Env.getGameController() != null)
        {
            Env.getGameController().init();
        }

        init();

        EventMgr.subscribe(Event.ID.settingsUpdated, event ->
        {
            ClientLocalizationManager loc = ((ClientLocalizationManager) LocalizationMgr);

            loc.updateLanguages();
            return false;
        });

        MusicMng.init();

        GamePadMgr = new GamePadManager();

        GamePadMgr.init();

        BrainOutClient.ClientController.init();

        if (Env.getTargetFullScreenDisplayMode() != null)
        {
            Gdx.graphics.setFullscreenMode(Env.getTargetFullScreenDisplayMode());
            Env.setTargetFullScreenDisplayMode(null);
        }
    }

    private void updateScreenSize(int width, int height)
    {
        screenWidth = width;
        screenHeight = height;
    }

    public static int getWidth()
    {
        return Gdx.graphics.getWidth();
    }

    public static int getHeight()
    {
        return Gdx.graphics.getHeight();
    }

    public static int getMin()
    {
        return Math.min(screenHeight, screenWidth);
    }

    public static TextureAtlas.AtlasRegion getRegion(String region)
    {
        if (region.equals("")) return null;
        try
        {
            return (TextureAtlas.AtlasRegion)Skin.getRegion(region);
        }
        catch (GdxRuntimeException ignored)
        {
            return null;
        }
    }

    public static NinePatch getNinePatch(String region)
    {
        if (region.equals("")) return null;
        return Skin.getPatch(region);
    }

    @Override
    public PackageManager getPackageManager(int threads)
    {
        return new ClientPackageManager(threads);
    }

    @Override
    public LocalizationManager getLocalizationManager()
    {
        return new ClientLocalizationManager();
    }

    @Override
    protected SkeletonJson newSkeleton()
    {
        return new SkeletonJson(new ProxyAtlasAttachmentLoader());
    }

    @Override
    public Controller getController()
    {
        return BrainOutClient.ClientController;
    }

    public static void drawFade(float alpha, Color color, Batch batch)
    {
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        ShapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        ShapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        ShapeRenderer.setColor(color.r, color.g, color.b, alpha);
        ShapeRenderer.rect(0, 0, 5000, 5000);
        ShapeRenderer.end();
    }

    public static void drawFade(float alpha, Batch batch)
    {
        drawFade(alpha, Color.BLACK, batch);
    }

    @Override
    public void init()
    {
        super.init();

        SocialController.init();
        Voice.init();

        initMainMenu().loadPackages();

        Gdx.app.postRunnable(this::postInit);
    }

    private void postInit()
    {
        if (!ClientSettings.IsFBOSupported() && !(Gdx.files.local("settings.json").exists()))
        {
            if (Log.INFO) Log.info("Old hardware! Forcing low graphics settings!");
            BrainOutClient.ClientSett.getGraphicsQuality().setValue(ClientSettings.GRAPHICS_VERY_LOW);
        }
    }

    @Override
    public void unloadContent(boolean unloadAssets, PackageManager.UnloadPackagesPredicate predicate)
    {
        super.unloadContent(unloadAssets, predicate);

        Env.getGameController().clear();
        ClientController.clear();
    }

    public MainMenuState initMainMenu()
    {
        Env.init();

        BrainOut.getInstance().unloadContent(false,
            contentPackage -> ClientConstants.Client.MAINMENU_PACKAGE.equals(contentPackage.getName()));

        BrainOut.PackageMgr.registerPackage(ClientConstants.Client.MAINMENU_PACKAGE);
        BrainOut.PackageMgr.checkValidation();

        MainMenuState mainMenuState = new MainMenuState();
        switchState(mainMenuState);

        return mainMenuState;
    }

    @Override
    public void dispose()
    {
        Voice.dispose();
        GamePadMgr.release();

        /*
        if (Analytics != null)
        {
            Analytics.eventSessionEnd();
            Analytics.store();
        }
        */

        Env.release();
        Avatars.Reset();

        if (ShapeRenderer != null)
        {
            ShapeRenderer.dispose();
        }

        if (ClientController.isConnected())
        {
            ClientController.disconnect(DisconnectReason.leave, 500);
        }

        BrainOut.Network.stop();

        if (SocialController != null)
        {
            SocialController.release();
            SocialController = null;
        }

        ContentMgr.unloadAllContent();

        Online.release();

        exit(0);
    }

    @Override
    public boolean hasTag(String tag)
    {
        return "client".equals(tag);
    }

    @Override
    public void packageLoaded(ContentPackage pack)
    {
        super.packageLoaded(pack);
    }

    private static float sampler = 0;

    @Override
    public void render()
    {
        if (!loop)
            return;

        _update.start();

        try
        {
            float dt = MathUtils.clamp(Gdx.graphics.getDeltaTime(), 0, 0.2f);

            if (hasState())
            {
                topState().update(dt);

                Gdx.gl.glClearColor(0, 0, 0, 1);
                Gdx.gl.glClear(GL30.GL_COLOR_BUFFER_BIT);

                _render.start();
                topState().render();
                if (Env.getGameController() != null)
                {
                    Env.getGameController().render();
                }
                _render.end();
            }

            Operations.update(dt);

            _events.start();
            EventMgr.update();
            _events.end();

            Env.update(dt);

            _controller.start();
            BrainOutClient.ClientController.update(dt);
            Actions.processActions(dt);
            _controller.end();

            /*
            if (Analytics != null)
            {
                Analytics.update(dt);
            }
            */

            GamePadMgr.update(dt);

        }
        catch (Exception e)
        {
            ExceptionHandler.handle(e);
        }

        _update.end();
    }

    @Override
    public void resize(int width, int height)
    {
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void pause()
    {
        //Gdx.graphics.setTitle("/// " + Env.getAppName());

        Env.pause();
    }

    @Override
    public void resume()
    {
        Gdx.graphics.setTitle(Env.getAppName());

        Env.resume();
    }

    public void pushState(final GameState gs)
    {
        if (hasState())
        {
            topState().onFocusOut();
        }

        statesStack.push(gs);

        gs.onInit();
        gs.onFocusIn();
    }

    public void popState()
    {
        if (hasState())
        {
            topState().onRelease();

            statesStack.pop();
        }

        if (hasState())
        {
            topState().onFocusIn();
        }
    }

    public boolean hasState()
    {
        return !statesStack.empty();
    }

    public GameState topState()
    {
        if (statesStack.empty())
            return null;

        return statesStack.peek();
    }

    public static void exit()
    {
        Gdx.app.exit();

        Runtime.getRuntime().halt(0);
    }

    public void switchState(GameState gameState)
    {
        while (hasState())
        {
            popState();
        }

        pushState(gameState);
    }

    @Override
    public TextureAtlas.AtlasRegion getTextureRegion(String region)
    {
        return getRegion(region);
    }

    @Override
    public GameMode newMode(GameMode.ID id)
    {
        switch (id)
        {
            case assault:
            {
                return new GameModeAssault(ClientAssaultRealization.class);
            }
            case foxhunt:
            {
                return new GameModeFoxHunt(ClientFoxHuntRealization.class);
            }
            case gungame:
            {
                return new GameModeGunGame(ClientGunGameRealization.class);
            }
            case zombie:
            {
                return new GameModeZombie(ClientZombieRealization.class);
            }
            case lobby:
            {
                return new GameModeLobby(ClientLobbyRealization.class);
            }
            case normal:
            {
                return new GameModeNormal(ClientNormalRealization.class);
            }
            case duel:
            {
                return new GameModeDuel(ClientDuelRealization.class);
            }
            case editor:
            {
                return new GameModeEditor(ClientEditorRealization.class);
            }
            case editor2:
            {
                return new GameModeEditor2(ClientEditor2Realization.class);
            }
            case domination:
            {
                return new GameModeDomination(ClientDominationRealization.class);
            }
            case deathmatch:
            {
                return new GameModeDeathmatch(ClientDeathmatchRealization.class);
            }
            case ctf:
            {
                return new GameModeCTF(ClientCTFRealization.class);
            }
            case free:
            {
                return new GameModeFree(ClientFreeRealization.class);
            }
        }

        return null;
    }

    @Override
    public Reward newReward()
    {
        return new ClientReward();
    }

    @Override
    public PlayState newPlayState(PlayState.ID id)
    {
        switch (id)
        {
            case game:
            {
                return new ClientPSGame();
            }
            case endgame:
            {
                return new ClientPSEndGame();
            }
            case empty:
            {
                return new ClientPSEmpty();
            }
        }

        return null;
    }

    @Override
    public void postRunnable(Runnable runnable)
    {
        Gdx.app.postRunnable(runnable);
    }

    @Override
    public void crashed(final Throwable e)
    {
        StringWriter result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);

        e.printStackTrace(printWriter);

        /*
        if (Analytics != null)
        {
            Analytics.eventError("critical", result.toString());
            Analytics.store();
        }
        */

        BrainOutClient.super.crashed(e);
    }

}
