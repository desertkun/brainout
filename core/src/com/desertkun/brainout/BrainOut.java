package com.desertkun.brainout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.*;

import com.desertkun.brainout.common.enums.OperationList;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.managers.*;
import com.desertkun.brainout.common.msg.Types;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.NetworkClient;
import com.desertkun.brainout.online.Reward;
import com.desertkun.brainout.packages.*;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.reflection.Reflection;
import com.desertkun.brainout.utils.*;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.minlog.Log;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public abstract class BrainOut
{
	protected static volatile BrainOut 			instance = null;

    public static SkeletonJson                  JsonSkeleton;
    public static SkeletonRenderer              SkeletonRndr;
    public static EventManager 				    EventMgr;
	public static ContentManager				ContentMgr;
    public static PackageManager                PackageMgr;
    public static OperationList                 Operations;
    public static LocalizationManager			LocalizationMgr;
    public static Environment                   Env;
    public static Random                        Rand;
    public static Reflection                    R;
    public static NetworkClient                 Network;
    public static Kryo                          Kryo;
    public static java.util.Timer               Timer;

    private static ObjectMap<String, Map>       MapDimensions = new ObjectMap<>();
    private static TimerTask ScheduledGC;

    protected boolean loop;

    public BrainOut(Environment env)
    {
        loop = true;

        Env = env;
        R = new Reflection();
        R.init();

        Kryo = new Kryo()
        {
            @Override
            public <T> T newInstance(Class<T> type)
            {
                return StaticMap.get(type);
            }
        };
    }

    public static BrainOut getInstance() {
        return instance;
    }

    public static void ScheduleGC()
    {
        if (ScheduledGC != null)
            return;

        ScheduledGC = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOut.getInstance().postRunnable(() ->
                {
                    ScheduledGC = null;
                    System.gc();
                });
            }
        };

        BrainOut.Timer.schedule(ScheduledGC, 5000);
    }

    protected abstract SkeletonJson newSkeleton();

    public abstract Controller getController();

    public void initCommonData(Kryo kryo)
    {
        Types.init(kryo);

        Timer = new Timer();

        LocalizationMgr = getLocalizationManager();
        Operations = new OperationList();
        Rand = new Random();

        ContentMgr = new ContentManager();
        EventMgr = new EventManager();

        int cores = Math.max(Runtime.getRuntime().availableProcessors() / 2, 1);

        PackageMgr = getPackageManager(cores);
    }

    public LocalizationManager getLocalizationManager()
    {
        return new LocalizationManager();
    }

    public void initData()
    {
        JsonSkeleton = newSkeleton();
        SkeletonRndr = new SkeletonRenderer();
        JsonSkeleton.setScale(1f / Constants.Graphics.RES_SIZE);
    }

    public void packageLoaded(ContentPackage pack)
    {
    }

	public void init()
	{
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                ExceptionHandler.handle(e);
            }
        });

        initData();
    }

    public static boolean IsServer()
    {
        Controller ctl = BrainOut.getInstance().getController();
        if (ctl == null)
            return false;

        return ctl.isServer();
    }

    public static boolean OnlineEnabled()
    {
        Controller ctl = BrainOut.getInstance().getController();
        if (ctl == null)
            return false;

        return ctl.isOnlineEnabled();
    }

    public boolean hasTag(String tag)
    {
        return false;
    }

    public static <T extends Map> Array<T> loadMapsFromStream(InputStream is, Class<T> classType)
    {
        return MapSerializer.LoadMaps(is, classType, null);
    }

    public static <T extends Map> Array<T> loadMapsFromStreamDimension(InputStream is, Class<T> classType, String dimension)
    {
        return MapSerializer.LoadMaps(is, classType, null, dimension);
    }

    public static <T extends Map> Array<T> loadMapsFromFile(String loadFile, Class<T> classType, String verify)
    {
        String mapsPath = System.getenv("BRAINOUT_MAPS");

        FileHandle fileHandle;
        if (mapsPath != null)
        {
            fileHandle = Gdx.files.absolute(mapsPath + "/" + Gdx.files.absolute(loadFile).name());
        }
        else
        {
            fileHandle = Gdx.files.absolute(loadFile);
        }

        InputStream is = fileHandle.read();

        return MapSerializer.LoadMaps(is, classType, verify);
    }

    public TextureAtlas.AtlasRegion getTextureRegion(String region)
    {
        return null;
    }

    public void unloadContent(boolean unloadAssets, PackageManager.UnloadPackagesPredicate predicate)
    {
        PackageMgr.unloadPackages(true, predicate);
    }

    public PackageManager getPackageManager(int threads)
    {
        return new PackageManager(threads);
    }

    public abstract GameMode newMode(GameMode.ID id);
    public abstract PlayState newPlayState(PlayState.ID id);
    public abstract Reward newReward();

    public abstract void postRunnable(Runnable runnable);

    public void crashed(Throwable e)
    {
        BrainOut.exit(1);
    }

    public static void exit(int status)
    {
        System.exit(status);
    }

    public static void StopLoop()
    {
        instance.loop = false;
    }
}
