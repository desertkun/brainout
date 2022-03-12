package com.desertkun.brainout.server.console;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public class MakeMod extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        client.setRights(PlayerRights.mod);

        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        if (asker == forClient)
        {
            return false;
        }

        return rights == PlayerRights.admin;
    }
}
