package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;

public class Code extends ConsoleCommand
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

        free.changeSafeCode(true, true);

        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return rights == PlayerRights.admin;
    }
}
