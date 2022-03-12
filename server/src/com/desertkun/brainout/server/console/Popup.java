package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.online.PlayerRights;

public class Popup extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        String text = args[1];

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            entry.value.showPopup("{MENU_ATTENTION}", text);
        }

        return "done";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        switch (rights)
        {
            case admin:
            case mod:
            case editor:
                return true;
            default:
                return false;
        }
    }
}
