package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.data.Data;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeAssault;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;
import com.desertkun.brainout.server.ServerController;
import com.esotericsoftware.minlog.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class ExtractExtensions extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        String path = args[1];

        try
        {
            Files.createDirectory(Paths.get(path));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "Cannot create directory";
        }

        Set<String> dimensions = new HashSet<>();

        for (Map map: Map.SafeAll())
        {
            dimensions.add(map.getDimension());
            for (ObjectMap.Entry<String, byte[]> entry : map.getExtensions())
            {
                try
                {
                    Files.write(Paths.get(path + "/" + entry.key), entry.value);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return "Cannot write file " + entry.key;
                }
            }

            map.getExtensions().clear();
        }


        final ServerController.MapSaveResult result = BrainOutServer.Controller.saveAll(
                BrainOutServer.Controller.getSuitableDimensions(client),
                Data.ComponentWriter.TRUE, true, -1, Runnable::run);

        if (result == null)
        {
            return "Map: cannot serialize map";
        }

        byte[] map = result.serialize();

        try
        {
            Files.write(Paths.get(path + "/extracted.map"), map);
        }
        catch (IOException e)
        {
            return "Cannot write map.";
        }

        return "done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        switch (rights)
        {
            case admin:
                return true;
            default:
                return false;
        }
    }
}
