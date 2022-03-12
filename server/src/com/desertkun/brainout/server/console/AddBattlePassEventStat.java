package com.desertkun.brainout.server.console;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.battlepass.BattlePassTaskData;
import com.desertkun.brainout.online.PlayerRights;
import com.desertkun.brainout.online.ServerBattlePassEvent;
import com.desertkun.brainout.online.ServerEvent;

public class AddBattlePassEventStat extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 2;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        String eventIndex = args[1];
        String amount = args[2];

        try
        {
            int eventIndexInt = Integer.valueOf(eventIndex);
            int amountInt = Integer.valueOf(amount);

            if (client instanceof PlayerClient)
            {
                PlayerClient playerClient = ((PlayerClient) client);

                ServerBattlePassEvent ev = null;

                for (ObjectMap.Entry<Integer, ServerEvent> event : playerClient.getOnlineEvents())
                {
                    if (!(event.value instanceof ServerBattlePassEvent))
                    {
                        continue;
                    }

                    ev = ((ServerBattlePassEvent) event.value);
                    break;
                }

                if (ev == null)
                    return "No active battle pass";

                if (eventIndexInt < 0 || eventIndexInt >= ev.getData().getTasks().size)
                    return "Bad index";

                ev.addProgress(ev.getData().getTasks().get(eventIndexInt), eventIndexInt, amountInt);

                return "Done";
            }
            else
            {
                return "Not a player";
            }
        }
        catch (NumberFormatException e)
        {
            return "Bad format";
        }
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return rights == PlayerRights.admin;
    }
}
