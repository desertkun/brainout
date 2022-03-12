package com.desertkun.brainout.server.console;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.bot.TaskFollowTargetForever;
import com.desertkun.brainout.bot.TaskHunter;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.BotControllerComponentData;
import com.desertkun.brainout.online.PlayerRights;

public class SpawnBotPlayer extends ConsoleCommand
{
    @Override
    public int requiredArgs()
    {
        return 0;
    }

    @Override
    public String execute(String[] args, Client client)
    {
        PlayerData currentPlayer = client.getPlayerData();

        if (currentPlayer == null)
        {
            return "You are dead.";
        }

        int amount;
        try
        {
            amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        }
        catch (NumberFormatException ignored)
        {
            amount = 1;
        }

        String playerName = args.length > 2 ? args[2] : currentPlayer.getContent().getID();
        String teamName = args.length > 3 ? args[3] : client.getTeam().getID();

        Content playerContent = BrainOutServer.ContentMgr.get(playerName);
        Content teamContent = BrainOutServer.ContentMgr.get(teamName);

        if (teamContent instanceof Team && playerContent instanceof Player)
        {
            Player player = ((Player) playerContent);
            Team team = (Team) teamContent;

            for (int i = 0; i < amount; i++)
            {
                Map map = currentPlayer.getMap();

                if (map == null)
                    return "No map";

                PlayerData playerData = (PlayerData)player.getData(map.getDimension());

                playerData.setTeam(team);

                BotControllerComponentData botController = new BotControllerComponentData(playerData);
                PlayerOwnerComponent ownerComponent = new PlayerOwnerComponent(playerData);
                PlayerRemoteComponent remoteComponent = new PlayerRemoteComponent(playerData);

                botController.getTasksStack().pushTask(new TaskHunter(botController.getTasksStack(), null));

                playerData.addComponent(botController);
                playerData.addComponent(ownerComponent);
                playerData.addComponent(remoteComponent);

                float spawnX = currentPlayer.getX(), spawnY = currentPlayer.getY();
                playerData.setPosition(spawnX, spawnY);

                /*
                for (ObjectMap.Entry<Slot, SlotItem.Selection> slotItem : shopCart.getItems())
                {
                    SlotItem.Selection selection = slotItem.value;
                    SlotItem item = selection.getItem();

                    boolean have = profile.hasItem(item);

                    if (have)
                    {
                        selection.apply(shopCart, playerData, profile, slotItem.key, slotItem.value);
                    }
                }
                */

                remoteComponent.setCurrentInstrument(ownerComponent.getCurrentInstrument());
                map.addActive(map.generateServerId(), playerData, true, true, ActiveData.ComponentWriter.TRUE);
            }

            return "Done";
        }

        return "No such team / player.";
    }

    @Override
    public boolean isRightsValid(Client asker, Client forClient, PlayerRights rights)
    {
        return rights == PlayerRights.admin;
    }
}
