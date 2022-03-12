package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.online.ClientProfile;
import com.desertkun.brainout.online.PlayerRights;

public class UnlockOwnable extends ConsoleCommand
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

        OwnableContent ownableContent = BrainOutServer.ContentMgr.get(id, OwnableContent.class);

        if (ownableContent != null)
        {
            int amount = 1;

            if (args.length >= 3)
            {
                try
                {
                    amount = Integer.valueOf(args[2]);
                }
                catch (NumberFormatException e)
                {
                    return "Bad format";
                }
            }

            if (client instanceof PlayerClient)
            {
                ((PlayerClient) client).gotOwnable(ownableContent, "console", ClientProfile.OnwAction.owned, amount);
                ((PlayerClient) client).sendUserProfile();
            }
            else
            {
                return "Not a player";
            }

            return "Done";
        }

        return "No such ownable.";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return rights == PlayerRights.admin;
    }
}
