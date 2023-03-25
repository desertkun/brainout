package com.desertkun.brainout.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.client.settings.ClientSettings;
import com.desertkun.brainout.client.settings.ScreenResolutionProperty;
import com.esotericsoftware.minlog.Log;

import java.io.*;


public class DesktopLauncher
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

        System.setProperty("java.awt.headless", "true");

        ClientEnvironment environment = new DesktopEnvironment(args);
        ClientSettings settings;

        try
        {
            settings = DesktopSettings.GetDesktopSettings(environment);
        }
        catch (Exception e)
        {
            e.printStackTrace();

            if (Log.ERROR) Log.error("Error loading settings: " + e.getMessage());
            return;
        }

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();

        String name = System.getenv("DESKTOP_PROFILE_NAME");

        if (name != null)
        {
            cfg.setTitle(environment.getAppName() + " - " + name);
        }
        else
        {
            cfg.setTitle(environment.getAppName());
        }

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

        if (System.getenv("FORCE_X") != null && System.getenv("FORCE_Y") != null)
        {
            cfg.setWindowPosition(Integer.valueOf(System.getenv("FORCE_X")),
                    Integer.valueOf(System.getenv("FORCE_Y")));
        }

        /*
        cfg.addIcon("icons/icon-128.png", Files.FileType.Internal);
        cfg.addIcon("icons/icon-64.png", Files.FileType.Internal);
        cfg.addIcon("icons/icon-32.png", Files.FileType.Internal);
        cfg.addIcon("icons/icon-16.png", Files.FileType.Internal);
         */

        cfg.useVsync(settings.getvSync().getValue());
        cfg.setAudioConfig(64, 512, 9);

        new Lwjgl3Application(BrainOutDesktop.initDesktopInstance(environment, settings), cfg);
    }
}
