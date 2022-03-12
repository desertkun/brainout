package com.desertkun.brainout.server.console;

import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.HealthComponentData;
import com.desertkun.brainout.online.PlayerRights;

public class God extends ConsoleCommand
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

        boolean enabled = true;

        if (args.length > 1)
        {
            enabled = args[1].equals("true");
        }

        if (playerData != null)
        {
            HealthComponentData hcd = playerData.getComponent(HealthComponentData.class);
            if (hcd != null)
            {
                hcd.setGod(enabled);
            }

            return "Done";
        }

        return "Player is dead.";
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
