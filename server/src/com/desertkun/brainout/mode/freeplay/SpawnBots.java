package com.desertkun.brainout.mode.freeplay;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.components.PlayerRemoteComponent;
import com.desertkun.brainout.components.ServerFreePartnerBotComponent;
import com.desertkun.brainout.content.PlayerSkin;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.quest.Quest;
import com.desertkun.brainout.content.quest.task.ServerEscortBot;
import com.desertkun.brainout.content.quest.task.ServerTask;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.MarkerData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.BotControllerComponentData;
import com.desertkun.brainout.data.components.PlayerAnimationComponentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.mode.payload.FreePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import org.json.JSONArray;
import org.json.JSONObject;

public class SpawnBots
{
    private Array<SpawnEntry> entries;
    private ObjectMap<String, ObjectMap<String, Array<ActiveData>>> spawnPool;

    private class SpawnEntry
    {
        private String botTask;
        private String groupId;
        private String spawnAt;
        private String player;
        private String spawnAtDimension;
        private PlayerSkin botSkin;
        private ItemsCollection give;
        private int amount;

        public SpawnEntry(JSONObject data)
        {
            amount = data.getInt("amount");
            give = new ItemsCollection(data.getJSONObject("give"));
            botTask = data.getString("bot-task");
            spawnAt = data.getString("spawn-at");
            groupId = data.getString("group-id");
            player = data.optString("player", "player-default");
            spawnAtDimension = data.getString("spawn-at-dimension");
            botSkin = BrainOutServer.ContentMgr.get(data.getString("skin"), PlayerSkin.class);
        }
    }

    public SpawnBots(JSONArray data)
    {
        this.entries = new Array<>();
        this.spawnPool = new ObjectMap<>();

        for (int i = 0, n = data.length(); i < n; i++)
        {
            SpawnEntry entry = new SpawnEntry(data.getJSONObject(i));

            this.entries.add(entry);
        }
    }

    public void spawn()
    {
        for (SpawnEntry entry : entries)
        {
            ObjectMap<String, Array<ActiveData>> d = spawnPool.get(entry.spawnAtDimension);
            if (d == null)
            {
                d = new ObjectMap<>();
                spawnPool.put(entry.spawnAtDimension, d);
            }

            Map map = Map.Get(entry.spawnAtDimension);

            if (map == null)
                continue;

            Array<ActiveData> s = d.get(entry.spawnAt);
            if (s == null)
            {
                s = new Array<>();
                d.put(entry.spawnAt, s);

                map.getActivesForTag(Constants.ActiveTags.MARKER, s, activeData ->
                {
                    if (!(activeData instanceof MarkerData))
                        return false;

                    return ((MarkerData) activeData).tag.equals(entry.spawnAt);
                });

                s.shuffle();
            }
        }

        for (SpawnEntry entry : entries)
        {
            Map map = Map.Get(entry.spawnAtDimension);

            if (map == null)
                continue;

            Array<ActiveData> spawnAt = spawnPool.get(entry.spawnAtDimension).get(entry.spawnAt);

            for (int i = 0; i < entry.amount; i++)
            {
                if (spawnAt.size == 0)
                    break;

                ActiveData spawnAt_ = spawnAt.pop();

                spawnBotAt(spawnAt_, entry);
            }
        }
    }

    private void spawnBotAt(ActiveData spawnAt_, SpawnEntry entry)
    {
        Player player = BrainOutServer.ContentMgr.get(entry.player, Player.class);
        Team team = BrainOutServer.ContentMgr.get("team-freeplay", Team.class);

        Map map = Map.Get(entry.spawnAtDimension);

        if (map == null)
            return;

        PlayerData playerData = (PlayerData)player.getData(map.getDimension());

        playerData.setTeam(team);

        BotControllerComponentData botController = new BotControllerComponentData(playerData);
        PlayerOwnerComponent ownerComponent = new PlayerOwnerComponent(playerData);
        PlayerRemoteComponent remoteComponent = new PlayerRemoteComponent(playerData);


        Task newTask = (Task) BrainOut.R.newInstance(
            entry.botTask, new Class[]{TaskStack.class},
            new Object[]{botController.getTasksStack()});

        botController.getTasksStack().pushTask(newTask);

        playerData.addComponent(new ServerFreePartnerBotComponent(entry.groupId));
        playerData.addComponent(botController);
        playerData.addComponent(ownerComponent);
        playerData.addComponent(remoteComponent);

        float spawnX = spawnAt_.getX(), spawnY = spawnAt_.getY();
        playerData.setPosition(spawnX, spawnY);
        playerData.setAngle(180);

        PlayerAnimationComponentData pac = playerData.getComponent(PlayerAnimationComponentData.class);
        pac.setSkin(entry.botSkin);

        entry.give.generate(ownerComponent.getConsumableContainer(), entry.spawnAtDimension, playerData);

        botController.selectFirstInstrument(ownerComponent);

        remoteComponent.setCurrentInstrument(ownerComponent.getCurrentInstrument());
        map.addActive(map.generateServerId(), playerData, true, true, ActiveData.ComponentWriter.TRUE);
    }
}
