package com.desertkun.brainout.server.console;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public class AddScore extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        String amount = args[1];

        try
        {
            int amountInt = Integer.valueOf(amount);

            client.addScore(amountInt, true);
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
