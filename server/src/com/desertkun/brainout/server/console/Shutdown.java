package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.PlayerRights;

public class Shutdown extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        boolean graceful = true;

        if (args.length > 1)
        {
            graceful = "true".equals(args[1]);
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode != null)
        {
            switch (gameMode.getID())
            {
                case lobby:
                default:
                {

                    BrainOutServer.TriggerShutdown();
                    return "Shutting lobby down...";
                }
            }
        }

        if (graceful)
        {
            BrainOutServer.Controller.gracefulShutdown();
            return "Shutting down will happen at the end of the game!";
        }
        else
        {
            BrainOutServer.TriggerShutdown();
            return "Shutting down...";
        }
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        switch (rights)
        {
            case admin:
                return true;
            default:
                return false;
        }
    }
}
