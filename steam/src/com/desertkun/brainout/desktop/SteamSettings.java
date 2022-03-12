package com.desertkun.brainout.desktop;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.ClientEnvironment;
import com.desertkun.brainout.client.settings.ClientSettings;

import java.io.File;

public class SteamSettings extends DesktopSettings
{
    public SteamSettings(ClientEnvironment environment)
    {
        super(environment);
    }

    public static ClientSettings GetSteamSettings(ClientEnvironment environment)
    {
        File file = new File("settings.json");

        SteamSettings clientSettings = new SteamSettings(environment);

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
}
