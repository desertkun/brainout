package com.desertkun.brainout.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.codedisaster.steamworks.SteamAPI;
import com.codedisaster.steamworks.SteamException;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.client.settings.ClientSettings;
import com.desertkun.brainout.client.settings.ScreenResolutionProperty;
import com.desertkun.brainout.desktop.client.states.CSSteamInit;
import com.esotericsoftware.minlog.Log;

import java.io.*;


public class SteamLauncher
{
    public static void main(String[] args)
    {
        try
        {
            System.setErr(new PrintStream(new FileOutputStream("error.log")));
        }
        catch (FileNotFoundException e)
        {
            //
        }

        CSSteamInit.TryInit();


        ClientEnvironment environment = new SteamEnvironment(args);
        ClientSettings settings;

        try
        {
            settings = SteamSettings.GetSteamSettings(environment);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            if (Log.ERROR) Log.error("Error loading settings: " + e.getMessage());
            return;
        }

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle(environment.getAppName());

        if (settings.getFullscreen().getValue())
        {
            ScreenResolutionProperty dm = settings.getDisplayMode();
            Graphics.DisplayMode selected = null;
            for (Graphics.DisplayMode displayMode : Lwjgl3ApplicationConfiguration.getDisplayModes())
            {
                if (dm.getWidth() == displayMode.width && dm.getHeight() == displayMode.height && dm.getHz() == displayMode.refreshRate
                        && dm.getBpp() == displayMode.bitsPerPixel)
                {
                    selected = displayMode;
                    break;
                }
            }

            if (selected == null)
            {
                for (Graphics.DisplayMode displayMode : Lwjgl3ApplicationConfiguration.getDisplayModes())
                {
                    if (displayMode.width < 1024)
                        continue;
                    if (displayMode.width >= 2200)
                        continue;
                    if (displayMode.refreshRate > 60)
                        continue;
                    if (selected != null && (selected.width > displayMode.width))
                        continue;
                    selected = displayMode;
                }
            }

            if (selected != null)
            {
                environment.setTargetFullScreenDisplayMode(selected);
            }

            cfg.setWindowedMode(1024, 768);
        }
        else
        {
            cfg.setWindowedMode(settings.getDisplayMode().getWidth(), settings.getDisplayMode().getHeight());
        }

        cfg.useVsync(settings.getvSync().getValue());
        cfg.setAudioConfig(64, 512, 9);

        try
        {
            Lwjgl3FileHandle h = new Lwjgl3FileHandle(new File(".prefs", "test"), Files.FileType.External);
            h.write(false);
        }
        catch (GdxRuntimeException exception)
        {
            if (Log.ERROR) Log.error("Cannot write into External folder, using Local instead!");
            cfg.setPreferencesConfig(".prefs", Files.FileType.Local);
        }

        cfg.setWindowIcon(Files.FileType.Internal,
        "icons/icon-128.png",
            "icons/icon-64.png",
            "icons/icon-32.png",
            "icons/icon-16.png");

        new Lwjgl3Application(BrainOutSteam.initSteamInstance(environment, settings), cfg);
    }
}
