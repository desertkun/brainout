package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public class KickPlayer extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        int user = Integer.valueOf(args[1]);

        Client toKick = BrainOutServer.Controller.getClients().get(user, null);

        if (toKick != null)
        {
            if (toKick.getRights() == PlayerRights.admin && client.getRights() != PlayerRights.admin)
            {
                return "Can't kick admin.";
            }

            toKick.kick("console command");

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
            case owner:
                return true;
            default:
                return false;
        }
    }
}
