package com.desertkun.brainout.server.console;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.online.PlayerRights;

public class UsePromo extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        if (!(client instanceof PlayerClient))
            return "Not a player.";

        PlayerClient playerClient = ((PlayerClient) client);

        playerClient.usePromo(args[1]);

        return "Requesting...";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return true;
    }
}
