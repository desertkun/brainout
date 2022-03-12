package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ActiveProgressComponentData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;

public class EnterFreeplay extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        if (!(client instanceof PlayerClient))
            return "Not a player";

        PlayerClient playerClient = ((PlayerClient) client);

        PlayerData playerData = playerClient.getPlayerData();

        if (playerData == null)
            return "Player not spawned";

        if (!playerData.getDimension().startsWith("intro"))
            return "not in intro";

        PlayState playState = BrainOutServer.Controller.getPlayState();

        if (playState.getID() != PlayState.ID.game)
            return "Not in game";

        GameMode mode = ((ServerPSGame) playState).getMode();

        if (mode.getID() != GameMode.ID.free)
            return "Not in freeplay";

        ServerFreeRealization free = ((ServerFreeRealization) mode.getRealization());

        free.playerEnter(playerData, playerClient);

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
