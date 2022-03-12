package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;

public class EndTime extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        PlayState playState = BrainOutServer.Controller.getPlayState();

        if (!(playState instanceof ServerPSGame))
        {
            return "Not in game";
        }

        ServerPSGame game = ((ServerPSGame) playState);

        try
        {
            final Integer time = Integer.parseInt(args[1]);

            BrainOutServer.PostRunnable(() ->
            {
                game.getMode().setEndTime(time);
                ((ServerRealization) game.getMode().getRealization()).updated();
            });
        }
        catch (NumberFormatException e)
        {
            return "Bad time.";
        }

        return "Done.";
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
