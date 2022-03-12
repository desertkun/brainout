package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.StringBuilder;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.online.PlayerRights;

public class NetworkStatistics extends ConsoleCommand
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

        long totalUdp = 0;
        long totalTcp = 0;

        long totalUdpRate = 0;
        long totalTcpRate = 0;

        data.append("---------- Network statistics ----------\n");

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client c = entry.value;

            if (c instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) c);

                PlayerClient.Statistics stats = playerClient.getStatistics();

                long udp = stats.getSentUdp();
                long tcp = stats.getSentTcp();
                long udpRate = stats.getSendUdpRate();
                long tcpRate = stats.getSendTcpRate();

                totalTcp += tcp; totalUdp += udp;
                totalTcpRate += tcpRate; totalUdpRate += udpRate;

                data.append("Client [").append(entry.key).append("] UDP: ").
                    append((float)udp / 1024.0f).append(" kb TCP: ").append((float)tcp / 1024.0f).append(" kb\n").
                    append("  Rate UDP: ").append((float)udpRate / 1024.0f).append(" kb/s TCP: ").append((float)tcpRate / 1024.0f).
                    append(" kb/s\n");
            }
        }

        data.append("---------------- Total -----------------\n");

        data.append("UDP: ").append((float)totalUdp / 1024.0f).append(" kb\n");
        data.append("TCP: ").append((float)totalTcp / 1024.0f).append(" kb\n");
        data.append("UDP rate: ").append((float)totalUdpRate / 1024.0f).append(" kb\n");
        data.append("TCP rate: ").append((float)totalTcpRate / 1024.0f).append(" kb\n");

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
