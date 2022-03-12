package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.enums.DisconnectReason;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerEditorRealization;
import com.desertkun.brainout.online.PlayerRights;

public class Migrate extends ConsoleCommand
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

        if (!(gameMode.getRealization() instanceof ServerEditorRealization))
            return "Not in editor";

        ServerEditorRealization editor = ((ServerEditorRealization) gameMode.getRealization());

        ServerMap map = Map.GetDefault(ServerMap.class);

        int index = 0;

        for (int j = 3, t = map.getBlocks().getBlockHeight(); j < t; j++)
        {
            for (int i = 0, k = map.getBlocks().getBlockWidth(); i < k; i++)
            {
                ChunkData chunk = map.getChunk(i, j);

                Array<BlockData> layer = chunk.getLayer(Constants.Layers.BLOCK_LAYER_FOREGROUND);

                if (layer == null)
                    continue;

                index++;
                String newName = map.getName() + "-" + index;

                ServerMap newMap = BrainOutServer.Controller.createMap(1, 1, newName);

                newMap.setName(map.getName());
                newMap.init();

                map.moveChunks(i, j, newMap, 0, 0, 1, 1);
            }
        }

        editor.redeliverMap();

        return "Moved " + index + " chunks.";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return true;
    }
}
