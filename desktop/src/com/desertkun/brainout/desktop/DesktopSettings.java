package com.desertkun.brainout.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.client.settings.ClientSettings;

import java.io.File;

public class DesktopSettings extends ClientSettings
{
    public DesktopSettings(ClientEnvironment environment)
    {
        super(environment);
    }

    public static ClientSettings GetDesktopSettings(ClientEnvironment environment)
    {
        File file = new File("settings.json");

        DesktopSettings clientSettings = new DesktopSettings(environment);

        clientSettings.init();

        FileHandle fileHandle = new FileHandle(file);

        if (fileHandle.exists())
        {
            JsonReader jsonReader = new JsonReader();
            JsonValue value = jsonReader.parse(fileHandle);

            clientSettings.read(new Json(), value);
        }

        return clientSettings;
    }

    @Override
    public Graphics.DisplayMode getDefaultDisplayMode()
    {
        return LwjglApplicationConfiguration.getDesktopDisplayMode();
    }

    @Override
    public Graphics.DisplayMode[] getDisplayModes()
    {
        return LwjglApplicationConfiguration.getDisplayModes();
    }
}
