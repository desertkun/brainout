package com.desertkun.brainout;

import android.content.Context;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.desertkun.brainout.controllers.GameController;
import com.desertkun.brainout.online.KryoNetworkClient;
import com.desertkun.brainout.online.NetworkClient;
import com.desertkun.brainout.online.NetworkConnectionListener;
import com.desertkun.brainout.packages.PackageManager;
import com.desertkun.brainout.utils.FileCopy;
import com.esotericsoftware.kryo.Kryo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class AndroidEnvironment extends ClientEnvironment
{
    private final Context context;
    private final AndroidGameController androidGameController;

    public AndroidEnvironment(Context context)
    {
        this.context = context;
        this.androidGameController = new AndroidGameController();
    }

    @Override
    public String getUniqueId()
    {
        try
        {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("getByIndex", String.class, String.class );
            return (String)(get.invoke(c, "ro.serialno", "unknown" ));
        }
        catch (Exception e)
        {
            return "";
        }
    }

    @Override
    public GameUser newUser()
    {
        return new GameUser();
    }

    @Override
    public Reflection getReflection()
    {
        return new ClientReflection()
        {
            @Override
            protected Object instantiate(Class clazz)
            {
                try
                {
                    Constructor<?> constructor = clazz.getConstructor(new Class[]{});
                    return constructor.newInstance(new Object[]{});
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public boolean instanceOf(Class classOff, Object object)
            {
                return classOff.isInstance(object);
            }
        };
    }

    @Override
    public String getExternalPath(String from)
    {
        return android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/brainout/" + from;
    }

    @Override
    public File getFile(String path)
    {
        return new File(path);
    }

    @Override
    public File getCacheDir()
    {
        return context.getCacheDir();
    }

    @Override
    public void init()
    {
        super.init();

        File mainMenu = new File(PackageManager.getPackagePath(ClientConstants.Client.MAINMENU_PACKAGE));
        FileHandle internal = Gdx.files.internal(PackageManager.packageFilename(ClientConstants.Client.MAINMENU_PACKAGE));

        if (!mainMenu.exists())
        {
            mainMenu.getParentFile().mkdirs();

            if (internal.exists())
            {
                try
                {
                    FileCopy.copyFile(internal.read(), mainMenu);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                // it's bad
                throw new RuntimeException("Mainmenu package is unable to copy.");
            }
        }
    }

    @Override
    public GameController getGameController()
    {
        return androidGameController;
    }

    @Override
    public NetworkClient newNetworkClient(Kryo kryo, NetworkConnectionListener listener)
    {
        return new KryoNetworkClient(kryo, listener);
    }
}
