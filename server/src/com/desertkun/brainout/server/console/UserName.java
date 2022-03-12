package com.desertkun.brainout.server.console;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public class UserName extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        client.setName(args[1]);

        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return (asker == forClient) || rights == PlayerRights.admin;
    }
}
