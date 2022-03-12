package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public class SpawnBot extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        int amount;
        try
        {
            amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        }
        catch (NumberFormatException ignored)
        {
            amount = 1;
        }

        int added = 0;

        for (int i = 0; i < amount; i++)
        {
            BotClient botClient = BrainOutServer.Controller.getClients().newBotClient(client.getTeam());

            if (botClient != null)
            {
                added++;
                botClient.init();
            }
        }

        return "Added bots: " + added;
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return rights == PlayerRights.admin;
    }
}
