package com.desertkun.brainout.server.console;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.online.PlayerRights;

public class SetStat extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 2;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        String item = args[1];
        String amount = args[2];

        try
        {
            float amountInt = Float.valueOf(amount);

            client.setStat(item, amountInt);

            if (client instanceof PlayerClient)
            {
                ((PlayerClient) client).sendUserProfile();
            }
        }
        catch (NumberFormatException e)
        {
            return "Bad format";
        }

        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return rights == PlayerRights.admin;
    }
}
