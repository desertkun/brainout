package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.online.PlayerRights;

public class CompleteQuest extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 1;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        String id = args[1];

        Quest quest = BrainOutServer.ContentMgr.get(id, Quest.class);

        if (quest != null)
        {
            if (client instanceof PlayerClient)
            {
                ModePayload payload = ((PlayerClient) client).getModePayload();

                if (payload instanceof FreePayload)
                {
                    ((FreePayload) payload).playerQuestComplete(quest);
                }
                else
                {
                    return "Not in freeplay";
                }
            }
            else
            {
                return "Not a player";
            }

            return "Done";
        }

        return "No such quest.";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return rights == PlayerRights.admin;
    }
}
