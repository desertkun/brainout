package com.desertkun.brainout.server.console;

import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.TaskFollowTarget;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.BotControllerComponentData;
import com.desertkun.brainout.online.PlayerRights;

public class FollowPlayer extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        if (!client.isAlive())
            return "Player is dead";

        PlayerData playerData = client.getPlayerData();

        Map map = playerData.getMap();

        ActiveData bot = map.getClosestActiveForTag(32, playerData.getX(), playerData.getY(),
                PlayerData.class, Constants.ActiveTags.PLAYERS,
                activeData -> activeData.getComponent(BotControllerComponentData.class) != null);

        if (bot == null)
            return "No bot nearby";

        BotControllerComponentData ctl = bot.getComponent(BotControllerComponentData.class);
        ctl.getTasksStack().pushTask(new TaskFollowTarget(ctl.getTasksStack(), playerData,
            (stack, enemy) -> false, false));

        return "Done";
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
