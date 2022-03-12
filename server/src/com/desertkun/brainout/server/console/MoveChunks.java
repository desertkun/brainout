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

public class MoveChunks extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 6;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (!(gameMode.getRealization() instanceof ServerEditorRealization))
            return "Not in editor";

        ServerEditorRealization editor = ((ServerEditorRealization) gameMode.getRealization());

        int fromX, fromY, toX, toY;

        String fromDimension = args[1];
        String toDimension = args[2];

        int width = 1;
        int height = 1;

        try
        {
            fromX = Integer.valueOf(args[3]);
            fromY = Integer.valueOf(args[4]);
            toX = Integer.valueOf(args[5]);
            toY = Integer.valueOf(args[6]);

            if (args.length >= 8)
            {
                width = Integer.valueOf(args[7]);
            }

            if (args.length >= 9)
            {
                height = Integer.valueOf(args[8]);
            }
        }
        catch (NumberFormatException e)
        {
            return "Bad format.";
        }

        ServerMap sourceMap = Map.Get(fromDimension, ServerMap.class);

        if (sourceMap == null)
            return "No such source dimension: " + fromDimension;

        ServerMap targetMap = Map.Get(toDimension, ServerMap.class);

        if (targetMap == null)
            return "No such target dimension: " + toDimension;

        if (!sourceMap.moveChunks(fromX, fromY, targetMap, toX, toY, width, height))
        {
            return "Failed to move.";
        }

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
