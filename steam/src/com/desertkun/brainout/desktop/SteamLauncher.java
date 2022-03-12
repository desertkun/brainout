package com.desertkun.brainout.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.client.settings.ClientSettings;
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

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = environment.getAppName();
        cfg.useGL30 = false;
        cfg.width = settings.getDisplayMode().getWidth();
        cfg.height = settings.getDisplayMode().getHeight();
        cfg.fullscreen = settings.getFullscreen().getValue();
        cfg.resizable = false;
        cfg.foregroundFPS = 60;
        cfg.allowSoftwareMode = true;
        cfg.audioDeviceSimultaneousSources = 64;
        cfg.audioDeviceBufferCount = 18;
        cfg.allowSoftwareMode = true;

        try
        {
            LwjglFileHandle h = new LwjglFileHandle(new File(".prefs", "test"), Files.FileType.External);
            h.write(false);
        }
        catch (GdxRuntimeException exception)
        {
            if (Log.ERROR) Log.error("Cannot write into External folder, using Local instead!");
            cfg.preferencesFileType = Files.FileType.Local;
        }

        cfg.addIcon("icons/icon-128.png", Files.FileType.Internal);
        cfg.addIcon("icons/icon-64.png", Files.FileType.Internal);
        cfg.addIcon("icons/icon-32.png", Files.FileType.Internal);
        cfg.addIcon("icons/icon-16.png", Files.FileType.Internal);


        cfg.vSyncEnabled = settings.getvSync().getValue();
        cfg.audioDeviceSimultaneousSources = 64;

        new LwjglApplication(BrainOutSteam.initSteamInstance(environment, settings), cfg);
    }
}
