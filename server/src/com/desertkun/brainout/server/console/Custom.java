package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.msg.server.editor.MapSettingsUpdatedMsg;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerEditorRealization;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.playstate.PlayState;
import com.desertkun.brainout.playstate.ServerPSGame;

public class Custom extends ConsoleCommand
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

        String dimension = args[1];

        ServerMap map = Map.Get(dimension, ServerMap.class);

        if (map == null)
            return "No such dimension";

        if (args.length > 3)
        {
            map.setCustom(args[2], args[3]);
        }
        else
        {
            String custom = map.getCustom(args[2]);
            if (custom == null)
                return "<Undefined>";
            return custom;
        }


        String data = BrainOut.R.JSON.toJson(map.getCustom());
        BrainOutServer.Controller.getClients().sendTCP(new MapSettingsUpdatedMsg(data, dimension));

        return "Done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return true;
    }
}
