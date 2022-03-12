package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public class ForEach extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client forUser = entry.value;

            if (!forUser.isInitialized())
                continue;

            String[] p = new String[args.length - 1];
            System.arraycopy(args, 1, p, 0, args.length - 1);

            BrainOutServer.Controller.getConsole().executeParams(forUser, p, client);
        }

        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return true;
    }
}
