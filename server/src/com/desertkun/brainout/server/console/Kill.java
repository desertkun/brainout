package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.GameModeAssault;
import com.desertkun.brainout.mode.ServerAssaultRealization;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;

public class Kill extends ConsoleCommand
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

        if (mode.getID() == GameMode.ID.free)
            return "Not allowed in freeplay";

        if (mode.getID() == GameMode.ID.assault)
        {
            GameModeAssault assault = (GameModeAssault)mode;
            GameModeAssault.State state = assault.getState();
            if (state == GameModeAssault.State.waiting)
                return "Not allowed while equipping";
        }

        if (client.getState() != Client.State.spawned)
        {
            return "player is not spawned";
        }

        if (client.getPlayerData() == null)
        {
            return "player is not inited";
        }

        client.kill();

        return "done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        if (asker == forClient)
        {
            return true;
        }

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
