package com.desertkun.brainout.content.quest.task;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.components.ServerFreePartnerBotComponent;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.BotControllerComponentData;
import com.desertkun.brainout.data.components.PlayerAnimationComponentData;
import com.desertkun.brainout.events.EnterPortalEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.mode.freeplay.ItemsCollection;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.EscortBot")
public class ServerEscortBot extends EscortBot implements ServerTask
{
    private String botTask;
    private String goTo;
    private PlayerSkin skin;
    private ItemsCollection give, placeNearby;
    private String spawnAt;
    private String placeNearbyAt;
    private String spawnAtDimension;

    @Override
    protected void readTask(JsonValue jsonData)
    {
        super.readTask(jsonData);

        give = new ItemsCollection(jsonData.get("give"));

        botTask = jsonData.getString("bot-task");
        goTo = jsonData.getString("go-to");
        skin = BrainOutServer.ContentMgr.get(jsonData.getString("skin"), PlayerSkin.class);
        spawnAt = jsonData.getString("spawn-at");
        placeNearbyAt = jsonData.getString("place-nearby-at", null);
        if (placeNearbyAt != null)
        {
            placeNearby = new ItemsCollection(jsonData.get("place-nearby"));
        }
        spawnAtDimension = jsonData.getString("spawn-at-dimension");
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case enterPortal:
            {
                EnterPortalEvent e = ((EnterPortalEvent) event);

                PlayerData playerData = e.playerData;
                PortalData enter = e.enter;

                GameMode gameMode = BrainOutServer.Controller.getGameMode();
                if (!(gameMode.getRealization() instanceof ServerFreeRealization))
                    return false;

                ServerFreeRealization free = ((ServerFreeRealization) gameMode.getRealization());

                Client client = BrainOutServer.Controller.getClients().get(playerData.getOwnerId());

                if (client instanceof PlayerClient)
                {
                    PlayerClient playerClient = ((PlayerClient) client);

                    if (enter.getDimension().equals(spawnAtDimension))
                    {
                        ModePayload modePayload = playerClient.getModePayload();
                        if (!(modePayload instanceof FreePayload))
                        {
                            return false;
                        }

                        FreePayload freePayload = ((FreePayload) modePayload);

                        if (getQuest().isCoop() && !freePayload.hasPartyMembers())
                        {
                            return false;
                        }

                        if (!freePayload.getCustomBool("escorted", false))
                        {
                            escort(freePayload, playerClient, playerData, free);
                        }

                    }
                }

                break;
            }
        }

        return false;
    }

    private void exited()
    {

    }

    private void escort(FreePayload freePayload, PlayerClient playerClient,
                        PlayerData currentPlayer, ServerFreeRealization free)
    {
        if (playerClient.getPartyId() == null || playerClient.getPartyId().isEmpty())
        {
            return;
        }

        ServerFreeRealization.Party party = free.getParty(playerClient.getPartyId());

        if (party == null)
            return;

        Player player = BrainOutServer.ContentMgr.get("player-default", Player.class);
        Team team = currentPlayer.getTeam();

        Map map = Map.Get(spawnAtDimension);

        if (map == null)
            return;

        ActiveData spawnAt_ = map.getActiveNameIndex().get(spawnAt);
        if (spawnAt_ == null)
            return;

        PlayerData playerData = (PlayerData)player.getData(map.getDimension());

        playerData.setTeam(team);

        BotControllerComponentData botController = new BotControllerComponentData(playerData);
        PlayerOwnerComponent ownerComponent = new PlayerOwnerComponent(playerData);
        PlayerRemoteComponent remoteComponent = new PlayerRemoteComponent(playerData);


        Task newTask = (Task)BrainOut.R.newInstance(
            botTask, new Class[]{TaskStack.class, String.class, PlayerData.class, Runnable.class},
            new Object[]{botController.getTasksStack(), goTo, currentPlayer,
            (Runnable) () ->
                BrainOutServer.PostRunnable(() ->
                    ServerTask.Trigger(ServerEscortBot.this, playerClient, 1))});

        botController.getTasksStack().pushTask(newTask);

        playerData.addComponent(new ServerFreePartnerBotComponent(playerClient.getPartyId()));
        playerData.addComponent(botController);
        playerData.addComponent(ownerComponent);
        playerData.addComponent(remoteComponent);

        float spawnX = spawnAt_.getX(), spawnY = spawnAt_.getY();
        playerData.setPosition(spawnX, spawnY);

        PlayerAnimationComponentData pac = playerData.getComponent(PlayerAnimationComponentData.class);
        pac.setSkin(skin);

        give.generate(ownerComponent.getConsumableContainer(), spawnAtDimension);

        botController.selectFirstInstrument(ownerComponent);

        if (placeNearbyAt != null)
        {
            for (ObjectMap.Entry<Integer, ActiveData> entry :
                map.getActives().getItemsForTag(Constants.ActiveTags.ITEM, true))
            {
                ActiveData activeData = entry.value;

                if (activeData instanceof ItemData)
                {
                    ItemData itemData = ((ItemData) activeData);

                    if (placeNearbyAt.equals(itemData.tag))
                    {
                        placeNearby.generate(itemData);
                        itemData.updated();
                        break;
                    }
                }
            }
        }

        remoteComponent.setCurrentInstrument(ownerComponent.getCurrentInstrument());
        map.addActive(map.generateServerId(), playerData, true, true, ActiveData.ComponentWriter.TRUE);

        for (ObjectMap.Entry<String, PlayerClient> entry : party.getMembers())
        {
            PlayerClient member = entry.value;

            ModePayload modePayload = member.getModePayload();
            if (!(modePayload instanceof FreePayload))
            {
                continue;
            }
            FreePayload f = ((FreePayload) modePayload);

            f.setCustomBool("escorted", true);
        }
    }

    @Override
    public void started(ServerFreeRealization free, PlayerClient playerClient)
    {

        //Object newTask = BrainOut.R.newInstance(botTask, new Class[]{TaskStack.class}, );
       ///if (!(newTask instanceof com.desertkun.brainout.content.quest.task.Task))
        //    throw new RuntimeException("Class " + clazz + " is not a task");
    }
}
