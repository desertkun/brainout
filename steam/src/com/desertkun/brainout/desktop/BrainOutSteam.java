package com.desertkun.brainout.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.Version;
import com.desertkun.brainout.client.settings.ClientSettings;
import com.desertkun.brainout.utils.ExceptionHandler;

public class BrainOutSteam extends BrainOutDesktop
{
    public BrainOutSteam(ClientEnvironment env, ClientSettings clientSettings)
    {
        super(env, clientSettings);
    }

    public static BrainOutClient initSteamInstance(ClientEnvironment env, ClientSettings clientSettings)
    {
        instance = new BrainOutDesktop(env, clientSettings);
        return getInstance();
    }

    public static BrainOutDesktop getInstance()
    {
        return (BrainOutDesktop)instance;
    }

    @Override
    public void init()
    {
        super.init();

        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> ExceptionHandler.handle(e));

        FileHandle version = Gdx.files.local("version.txt");
        try
        {
            version.writeString(Version.VERSION, false);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
