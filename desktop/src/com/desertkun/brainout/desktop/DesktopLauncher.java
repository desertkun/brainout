package com.desertkun.brainout.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.AudioRecorder;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.audio.LwjglAudio;
import com.badlogic.gdx.files.FileHandle;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.client.settings.ClientSettings;
import com.esotericsoftware.minlog.Log;

import java.io.*;


public class DesktopLauncher
{
    private static class LwjglAudioWrapper implements LwjglAudio
    {
        private final LwjglAudio parent;

        public LwjglAudioWrapper(LwjglAudio parent)
        {
            this.parent = parent;
        }

        @Override
        public void update()
        {
            this.parent.update();
        }

        @Override
        public AudioDevice newAudioDevice(int samplingRate, boolean isMono)
        {
            return this.parent.newAudioDevice(samplingRate, isMono);
        }

        @Override
        public AudioRecorder newAudioRecorder(int samplingRate, boolean isMono)
        {
            return this.parent.newAudioRecorder(samplingRate, isMono);
        }

        @Override
        public Sound newSound(FileHandle fileHandle)
        {
            return this.parent.newSound(fileHandle);
        }

        @Override
        public Music newMusic(FileHandle file)
        {
            return this.parent.newMusic(file);
        }

        @Override
        public void dispose()
        {
            if (System.getenv("BRAINOUT_NO_SOUND") == null)
            {
                this.parent.dispose();
            }
        }
    }

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

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = environment.getAppName();

        String name = System.getenv("DESKTOP_PROFILE_NAME");

        if (name != null)
        {
            cfg.title += " - " + name;
        }

        cfg.useGL30 = false;
        cfg.width = settings.getDisplayMode().getWidth();
        cfg.height = settings.getDisplayMode().getHeight();
        cfg.fullscreen = settings.getFullscreen().getValue();
        cfg.resizable = false;
        cfg.foregroundFPS = 60;
        cfg.allowSoftwareMode = true;
        cfg.audioDeviceSimultaneousSources = 64;
        cfg.audioDeviceBufferCount = 18;

        if (System.getenv("FORCE_X") != null)
        {
            cfg.x = Integer.valueOf(System.getenv("FORCE_X"));
        }

        if (System.getenv("FORCE_Y") != null)
        {
            cfg.y = Integer.valueOf(System.getenv("FORCE_Y"));
        }

        /*
        cfg.addIcon("icons/icon-128.png", Files.FileType.Internal);
        cfg.addIcon("icons/icon-64.png", Files.FileType.Internal);
        cfg.addIcon("icons/icon-32.png", Files.FileType.Internal);
        cfg.addIcon("icons/icon-16.png", Files.FileType.Internal);
         */

        cfg.vSyncEnabled = settings.getvSync().getValue();
        cfg.audioDeviceSimultaneousSources = 64;

        new LwjglApplication(BrainOutDesktop.initDesktopInstance(environment, settings), cfg)
        {
            @Override
            public LwjglAudio createAudio(LwjglApplicationConfiguration config)
            {
                return new LwjglAudioWrapper(super.createAudio(config));
            }
        };
    }
}
