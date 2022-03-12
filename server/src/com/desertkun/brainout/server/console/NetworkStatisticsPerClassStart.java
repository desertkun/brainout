package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.online.PlayerRights;

public class NetworkStatisticsPerClassStart extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        StringBuilder data = new StringBuilder();

        data.append("---------- Network statistics ----------\n");

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client c = entry.value;

            if (c instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) c);
                PlayerClient.Statistics stats = playerClient.getStatistics();
                stats.setCollectPerClass();
            }
        }

        return "OK";
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
