package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.ClientList;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;

public class KillTeam extends ConsoleCommand
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

        Team doomedTeam = null;

        if (args.length > 1 && args[1] != null)
        {
            Array<Team> teams = BrainOutServer.Controller.getTeams();
            for (Team team : teams)
            {
                if (team.getID().equals(args[1]))
                    doomedTeam = team;
            }

            if (doomedTeam == null)
                return "Can't find team";
        }
        else
            doomedTeam = client.getTeam();

        ClientList clients = BrainOutServer.Controller.getClients();

        for(Client player : clients.values())
        {
            if (player.getTeam() == doomedTeam)
            {
                if (player.getState() != Client.State.spawned)
                {
                    continue;
                }

                if (player.getPlayerData() == null)
                {
                    continue;
                }

                player.kill();
            }
        }

        return "done";
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
