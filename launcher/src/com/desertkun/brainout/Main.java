package com.desertkun.brainout;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import java.awt.*;
import java.lang.reflect.Method;
import java.net.URL;

public class Main
{
    public static void main(String[] args)
    {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Brain / Out";
        cfg.useGL30 = false;
        cfg.width = 569;
        cfg.height = 320;
        cfg.fullscreen = false;
        cfg.resizable = false;
        cfg.allowSoftwareMode = true;

        cfg.addIcon("assets/icon-128.png", Files.FileType.Internal);
        cfg.addIcon("assets/icon-64.png", Files.FileType.Internal);
        cfg.addIcon("assets/icon-32.png", Files.FileType.Internal);
        cfg.addIcon("assets/icon-16.png", Files.FileType.Internal);

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac"))
        {
            try {
                Class util = Class.forName("com.apple.eawt.Application");
                Method getApplication = util.getMethod("getApplication", new Class[0]);
                Object application = getApplication.invoke(util);
                Class params[] = new Class[1];
                params[0] = Image.class;
                Method setDockIconImage = util.getMethod("setDockIconImage", params);
                URL url = Main.class.getClassLoader().getResource("assets/icon-64.png");
                Image image = Toolkit.getDefaultToolkit().getImage(url);
                setDockIconImage.invoke(application, image);
            } catch (Exception e) {
                //
            }
        }

        cfg.vSyncEnabled = false;
        cfg.audioDeviceSimultaneousSources = 64;

        new LwjglApplication(new Launcher(args), cfg);
    }
}
