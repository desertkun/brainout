package com.desertkun.brainout.server.console;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public class Echo extends ConsoleCommand
{
    public Echo() {}

    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        StringBuilder builder = new StringBuilder();

        builder.append("hello. Args: ( ");
        for(String s : args)
        {
            builder.append(s).append(" ");
        }
        builder.append(")");
        return builder.toString();
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return true;
    }
}
