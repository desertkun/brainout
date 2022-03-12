package com.desertkun.brainout.server.console;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.online.PlayerRights;

public class ResetProfile extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        if (client instanceof PlayerClient)
        {
            PlayerClient playerClient = ((PlayerClient) client);

            playerClient.getProfile().reset();
            playerClient.getProfile().setDirty();
        }
        else
        {
            return "Not a player";
        }

        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return rights == PlayerRights.admin;
    }
}
