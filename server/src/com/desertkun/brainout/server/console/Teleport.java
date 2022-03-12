package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.online.PlayerRights;

public class Teleport extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        int user;

        try
        {
            user = Integer.valueOf(args[1]);
        }
        catch (NumberFormatException e)
        {
            user = -1;
        }

        if (user >= 0)
        {
            Client teleportTo = BrainOutServer.Controller.getClients().get(user, null);

            if (teleportTo != null)
            {
                PlayerData toPlayer = teleportTo.getPlayerData();

                if (toPlayer != null)
                {
                    if (client instanceof PlayerClient)
                    {
                        PlayerClient playerClient = ((PlayerClient) client);
                        playerClient.moveTo(toPlayer.getDimension(), toPlayer.getX(), toPlayer.getY());
                    }
                }

                return "Done.";
            }
        }
        else
        {
            ActiveData item = null;

            for (Map map : Map.SafeAll())
            {
                item = map.getActiveNameIndex().get(args[1]);

                if (item != null)
                    break;
            }

            if (item == null)
                return "No such item";

            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);
                playerClient.moveTo(item.getDimension(), item.getX(), item.getY());
            }

            return "Done.";
        }

        return "User not found.";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        switch (rights)
        {
            case admin:
            case mod:
                return true;
            default:
                return false;
        }
    }
}
