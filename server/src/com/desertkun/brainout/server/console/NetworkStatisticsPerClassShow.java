package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.online.PlayerRights;

import java.util.Comparator;

public class NetworkStatisticsPerClassShow extends ConsoleCommand
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

        ObjectMap<String, Integer> total = new OrderedMap<>();

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client c = entry.value;

            if (c instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) c);

                PlayerClient.Statistics stats = playerClient.getStatistics();

                for (ObjectMap.Entry<String, Integer> sent : stats.getSentPerClass())
                {
                    total.put(sent.key, total.get(sent.key, 0) + sent.value);
                }
            }
        }

        Array<String> keys = total.keys().toArray();
        keys.sort((o1, o2) -> total.get(o2) - total.get(o1));

        data.append("---------------- Total -----------------\n");

        for (int i = 0; i < Math.min(keys.size, 16); i++)
        {
            String k = keys.get(i);
            data.append(k).append(" = ").append(String.valueOf(total.get(k))).append("\n");

        }

        data.append("----------------------------------------\n");


        return data.toString();
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
