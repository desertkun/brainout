package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public class GameSpeed extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        try
        {
            final Float speed = Float.parseFloat(args[1]);

            BrainOutServer.PostRunnable(new Runnable()
            {
                @Override
                public void run()
                {
                    BrainOutServer.Controller.setSpeed(speed);
                }
            });
        }
        catch (NumberFormatException e)
        {
            return "Bad speed.";
        }

        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        switch (rights)
        {
            case admin:
            case mod:
                return true;
        }

        return false;
    }
}
