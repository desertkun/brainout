package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerEditorRealization;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;

public class ResizeMap extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 2;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (!(gameMode.getRealization() instanceof ServerEditorRealization))
            return "Not in editor";

        ServerEditorRealization editor = ((ServerEditorRealization) gameMode.getRealization());

        int width, height;

        String dimension = args[1];

        int alignX = 0;
        int alignY = 0;

        try
        {
            width = Integer.valueOf(args[2]);
            height = Integer.valueOf(args[3]);

            if (args.length >= 5)
            {
                alignX = Integer.valueOf(args[4]);
            }

            if (args.length >= 6)
            {
                alignY = Integer.valueOf(args[5]);
            }
        }
        catch (NumberFormatException e)
        {
            return "Bad format.";
        }

        ServerMap map = Map.Get(dimension, ServerMap.class);

        map.resize(width, height, alignX, alignY);

        editor.redeliverMap();

        return "Done.";
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
