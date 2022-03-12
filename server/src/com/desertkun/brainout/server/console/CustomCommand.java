package com.desertkun.brainout.server.console;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.online.PlayerRights;

public class CustomCommand extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        PlayerData playerData = client.getPlayerData();
        Active spot;
        spot = BrainOutServer.ContentMgr.get("cold-blizzard", Active.class);
        Map map = Map.Get(playerData.getDimension());

        if (map == null)
            return "error";

        ActiveData a = spot.getData(playerData.getDimension());

        a.setPosition(playerData.getX(), playerData.getY());

        map.addActive(map.generateServerId(), a, true, true);

        return "puk";
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
