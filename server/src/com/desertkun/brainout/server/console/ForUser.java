package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public class ForUser extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 2;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        int user;

        try
        {
            user = Integer.valueOf(args[1]);
        }
        catch (NumberFormatException ignored)
        {
            return "Bad user id.";
        }

        Client forUser = BrainOutServer.Controller.getClients().get(user, null);

        if (forUser != null)
        {
            String[] p = new String[args.length - 2];
            System.arraycopy(args, 2, p, 0, args.length - 2);

            return BrainOutServer.Controller.getConsole().executeParams(forUser, p, client);
        }

        return "User not found";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return asker == forClient;
    }
}
