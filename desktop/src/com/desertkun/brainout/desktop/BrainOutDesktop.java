package com.desertkun.brainout.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.desertkun.brainout.*;
import com.desertkun.brainout.client.settings.ClientSettings;
import com.desertkun.brainout.utils.ExceptionHandler;
import com.esotericsoftware.minlog.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

public class BrainOutDesktop extends BrainOutClient
{
    public BrainOutDesktop(ClientEnvironment env, ClientSettings clientSettings)
    {
        super(env, clientSettings);
    }

    public static BrainOutClient initDesktopInstance(ClientEnvironment env, ClientSettings clientSettings)
    {
        instance = new BrainOutDesktop(env, clientSettings);
        return getInstance();
    }

    public static BrainOutDesktop getInstance()
    {
        return (BrainOutDesktop)instance;
    }

    private static boolean lockInstance(final String lockFile) {
        try {
            final File file = new File(lockFile);
            final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            final FileLock fileLock = randomAccessFile.getChannel().tryLock();

            if (fileLock != null)
            {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            fileLock.release();
                            randomAccessFile.close();
                            file.delete();
                        }
                        catch (Exception e)
                        {
                            Log.error("Unable to remove lock file: " + lockFile);
                        }
                    }
                });

                return true;
            }
        }
        catch (Exception e)
        {
            Log.error("Unable to create and/or lock file: " + lockFile);
        }
        return false;
    }

    @Override
    public void create()
    {
        super.create();

        if (Constants.Core.MULTIPLE_WINDOWS_DISABLED)
        {
            FileHandle lock = Gdx.files.external(".l");
            String path;

            try
            {
                path = lock.file().getAbsolutePath();
            }
            catch (Exception e)
            {
                return;
            }
            
            if (!lockInstance(path))
            {
                if (Log.ERROR) Log.error("Running multiple windows is not allowed");
                BrainOut.exit(-1);
            }
        }
    }

    @Override
    public void init()
    {
        super.init();

        Thread.currentThread().setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread t, Throwable e)
            {
                ExceptionHandler.handle(e);
            }
        });

        try
        {
            FileHandle version = Gdx.files.local("version.txt");
            version.writeString(Version.VERSION, false);
        }
        catch (Exception ignored)
        {
        }
    }
}
