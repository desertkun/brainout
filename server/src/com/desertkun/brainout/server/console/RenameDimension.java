package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerEditorRealization;
import com.desertkun.brainout.online.PlayerRights;

public class RenameDimension extends ConsoleCommand
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

        String dimension = args[1];

        ServerMap map = Map.Get(dimension, ServerMap.class);

        if (map == null)
            return "No such dimension";


        String dimensionTo = args[2];

        if (Map.Get(dimensionTo, ServerMap.class) != null)
            return "Such dimension already exists";

        map.setDimension(dimensionTo);
        for (ObjectMap.Entry<Integer, ActiveData> entry : map.getActives())
        {
            entry.value.setDimension(dimensionTo);
        }

        map.getBlocks().setDimension(dimensionTo);

        for (int j = 0, t = map.getBlocks().getBlockHeight(); j < t; j++)
        {
            for (int i = 0, k = map.getBlocks().getBlockWidth(); i < k; i++)
            {
                ChunkData chunkData = map.getChunk(i, j);

                if (chunkData == null)
                    continue;

                chunkData.updateInfo(i, j, map.getBlocks());
            }
        }

        Map.Move(dimension, dimensionTo);

        editor.redeliverMap();

        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return true;
    }
}
