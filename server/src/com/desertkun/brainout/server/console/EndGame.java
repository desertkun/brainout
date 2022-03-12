package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;

public class EndGame extends ConsoleCommand
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

        if (playState instanceof ServerPSGame)
        {
            ServerPSGame game = ((ServerPSGame) playState);
            game.getGameResult().setTeamWon(null);
            game.getGameResult().setPlayerWon(-1);
            game.endGame();

            return "Done";
        }

        return "Playstate is not in game.";
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
