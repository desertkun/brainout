package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.active.Active;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.FreeplayPlayerComponentData;
import com.desertkun.brainout.data.components.ServerFreeplayPlayerComponentData;
import com.desertkun.brainout.online.PlayerRights;

public class FullHealCommand extends ConsoleCommand
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
        ServerFreeplayPlayerComponentData sfpc = playerData.getComponent(ServerFreeplayPlayerComponentData.class);
        if (sfpc != null)
        {
            sfpc.fullRecovery();
            return "You are healed!";
        }

        return "FreeplayPlayerComponentData not found";
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
