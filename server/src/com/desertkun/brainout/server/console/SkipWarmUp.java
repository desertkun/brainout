package com.desertkun.brainout.server.console;


import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerRealization;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;

public class SkipWarmUp extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode != null)
        {
            ServerRealization serverRealization = ((ServerRealization) gameMode.getRealization());
            serverRealization.skipWarmUp();

            return "Done.";
        }

        return "Not in game.";
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
