package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;

public class Rain extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        PlayState playState = BrainOutServer.Controller.getPlayState();

        if (playState.getID() != PlayState.ID.game)
            return "Not in game";

        GameMode mode = ((ServerPSGame) playState).getMode();

        if (mode.getID() != GameMode.ID.free)
            return "Not in freeplay";

        ServerFreeRealization free = ((ServerFreeRealization) mode.getRealization());

        int offset;
        try
        {
            offset = args.length > 1 ? Integer.parseInt(args[1]) : 0;
        }
        catch (NumberFormatException ignored)
        {
            offset = 0;
        }

        long seconds = System.currentTimeMillis() / 1000L;
        long rainCycle = Constants.Core.SECONDS_IN_A_DAY * 5;
        long off = seconds % rainCycle;

        free.setTimeOfDayOffset((int)-off + offset);

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
            default:
                return false;
        }
    }
}
