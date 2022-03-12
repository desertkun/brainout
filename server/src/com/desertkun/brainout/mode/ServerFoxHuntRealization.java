package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskHunter;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.DefaultConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.mode.payload.FoxHuntPayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.playstate.PlayStateEndGame;
import com.desertkun.brainout.playstate.ServerPSEndGame;

import java.util.TimerTask;

public class ServerFoxHuntRealization extends ServerRealization<GameModeFoxHunt>
{
    private int ticketsForNewPlayer;

    public ServerFoxHuntRealization(GameModeFoxHunt gameMode)
    {
        super(gameMode);

        this.ticketsForNewPlayer = 0;
    }

    @Override
    public boolean canDropConsumable(Client playerClient, ConsumableItem item)
    {
        return false;
    }

    @Override
    public boolean isDeathDropEnabled(PlayerData playerData)
    {
        return false;
    }

    @Override
    public boolean awardScores()
    {
        return false;
    }

    public Client getFox()
    {
        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client = entry.value;

            ModePayload payload = client.getModePayload();

            if (payload instanceof FoxHuntPayload)
            {
                if (((FoxHuntPayload) payload).isFox())
                {
                    return client;
                }
            }
        }

        return null;
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        Array<Client> clients = BrainOutServer.Controller.getClients().values().toArray();
        clients.sort((o1, o2) -> o1.getScore() < o2.getScore() ? 1 : -1);

        if (clients.size > 0)
        {
            gameResult.setPlayerWon(clients.get(0).getId());
        }

        if (gameResult instanceof ServerPSEndGame.ServerGameResult)
        {
            ServerPSEndGame.ServerGameResult serverGameResult = ((ServerPSEndGame.ServerGameResult) gameResult);
            serverGameResult.getRewardClients().clear();

            for (int i = 0; i < (float)clients.size / 2.0f; i++)
            {
                serverGameResult.getRewardClients().add(clients.get(i));
            }
        }

        return true;
    }

    @Override
    public boolean isComplete(PlayStateEndGame.GameResult gameResult)
    {
        if (getGameMode().getTickets() <= 0)
        {
            Array<Client> clients = BrainOutServer.Controller.getClients().values().toArray();
            clients.sort((o1, o2) -> o1.getScore() < o2.getScore() ? 1 : -1);

            if (clients.size > 0)
            {
                gameResult.setPlayerWon(clients.get(0).getId());
            }

            if (gameResult instanceof ServerPSEndGame.ServerGameResult)
            {
                ServerPSEndGame.ServerGameResult serverGameResult = ((ServerPSEndGame.ServerGameResult) gameResult);
                serverGameResult.getRewardClients().clear();

                for (int i = 0; i < (float)clients.size / 2.0f; i++)
                {
                    serverGameResult.getRewardClients().add(clients.get(i));
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.ticketsForNewPlayer = jsonData.getInt("ticketsForNewPlayer", 0);

        int tickets = jsonData.getInt("tickets", 0);
        getGameMode().setInitialTickets(tickets);
    }

    @Override
    public void finished()
    {
        BrainOutServer.Controller.setSpeed(0.5f);

        BrainOut.Timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOutServer.PostRunnable(() -> BrainOutServer.Controller.setSpeed(1f));
            }
        }, 4000);
    }

    @Override
    public void clientInitialized(Client client, boolean reconnected)
    {
        super.clientInitialized(client, reconnected);

        if (!client.isAlive())
        {
            client.requestSpawn();
        }
    }

    @Override
    protected void maxPlayersIncreased(int diff)
    {
        int addPoints = diff * ticketsForNewPlayer;

        getGameMode().setTickets(getGameMode().getTickets() + addPoints);
        getGameMode().setInitialTickets(getGameMode().getInitialTickets() + addPoints);

        updated();
    }

    @Override
    public ModePayload newPlayerPayload(Client playerClient)
    {
        return new FoxHuntPayload(playerClient);
    }

    private void setPlayerAsFox(Client player)
    {
        PlayerData playerData = player.getPlayerData();

        if (playerData == null)
            return;

        ModePayload payload = player.getModePayload();

        if (!(payload instanceof FoxHuntPayload))
            return;

        FoxHuntPayload foxHuntPayload = ((FoxHuntPayload) payload);
        foxHuntPayload.setFox(true);

        PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);

        Queue<ConsumableRecord> toRemove = new Queue<>();
        Slot melee = BrainOut.ContentMgr.get("slot-melee", Slot.class);

        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : poc.getConsumableContainer().getData())
        {
            ConsumableRecord record = entry.value;
            ConsumableItem item = record.getItem();

            if (item instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
                InstrumentData instrumentData = ici.getInstrumentData();
                if (instrumentData.getInstrument().getSlot() == melee)
                {
                    continue;
                }

                toRemove.addLast(record);
            }
        }

        for (ConsumableRecord record : toRemove)
        {
            poc.getConsumableContainer().decConsumable(record, record.getAmount());
        }

        Weapon weapon = BrainOutServer.ContentMgr.get("weapon-hunter-bow", Weapon.class);
        WeaponData weaponData = weapon.getData(playerData.getDimension());
        weaponData.setSkin(weapon.getDefaultSkin());

        InstrumentConsumableItem ici = new InstrumentConsumableItem(weaponData, playerData.getDimension());
        ConsumableRecord record = poc.getConsumableContainer().putConsumable(1, ici);

        {
            Bullet bullet =  BrainOutServer.ContentMgr.get("arrow-bullet", Bullet.class);
            poc.getConsumableContainer().putConsumable(200, new DefaultConsumableItem(bullet));
        }

        ServerPlayerControllerComponentData ctl =
            playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

        ctl.selectFirstInstrument(poc);
        ctl.updateAttachments();
        ctl.consumablesUpdated();
        ctl.instrumentSelected(record);
        ctl.changeInstrument(record.getId());

        updated();
    }

    @Override
    public void write(Json json, int owner)
    {
        super.write(json, owner);

        Client fox = getFox();

        if (fox != null)
        {
            json.writeValue("fox", fox.getId());
        }
    }

    @Override
    public void onClientSpawn(Client client, PlayerData player)
    {
        super.onClientSpawn(client, player);

        if (!getGameMode().isGameActive())
            return;

        Client fox = getFox();
        if (fox == null)
        {
            setPlayerAsFox(client);
        }
        else
        {
            if (fox == client)
            {
                setPlayerAsFox(client);
            }
        }
    }

    @Override
    public void clientReleased(Client client)
    {
        super.clientReleased(client);

        Client fox = getFox();

        if (fox == client)
        {
            ModePayload foxPayload = fox.getModePayload();

            if (foxPayload instanceof FoxHuntPayload)
            {
                FoxHuntPayload foxHuntPayload = ((FoxHuntPayload) foxPayload);
                foxHuntPayload.setFox(false);
            }

            setRandomPlayerAsFox(fox);
        }
    }

    @Override
    public void onClientDeath(Client client, Client killer, PlayerData playerData, InstrumentInfo info)
    {
        super.onClientDeath(client, killer, playerData, info);

        if (!getGameMode().isGameActive())
            return;

        boolean trackStats = client instanceof PlayerClient && killer instanceof PlayerClient;

        if (!trackStats)
        {
            PlayerClient who;

            if (client instanceof PlayerClient)
            {
                who = ((PlayerClient) client);
            }
            else if (killer instanceof PlayerClient)
            {
                who = ((PlayerClient) killer);
            }
            else
            {
                // literally not possible
                who = null;
            }

            trackStats = who != null && who.catTrackStatsWithBots();
        }

        Client fox = getFox();

        if (fox != null)
        {
            if (fox == client)
            {
                ModePayload foxPayload = fox.getModePayload();

                if (foxPayload instanceof FoxHuntPayload)
                {
                    FoxHuntPayload foxHuntPayload = ((FoxHuntPayload) foxPayload);
                    foxHuntPayload.setFox(false);
                }

                if (killer == null || killer == fox)
                {
                    setRandomPlayerAsFox(fox);
                }
                else
                {
                    killer.addScore(10, trackStats);
                    getGameMode().setTickets(getGameMode().getTickets() - 1);
                    setPlayerAsFox(killer);
                }
            }
            else
            {
                if (killer == fox)
                {
                    killer.addScore(20, trackStats);
                    getGameMode().setTickets(getGameMode().getTickets() - 1);
                }
            }
        }

        updated();
    }

    private void setRandomPlayerAsFox(Client fox)
    {
        Array<Client> clientList = new Array<>();

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client client1 = entry.value;

            if (client1 == fox)
                continue;

            if (!client1.isAlive())
                continue;

            clientList.add(client1);
        }

        if (clientList.size > 0)
        {
            setPlayerAsFox(clientList.random());
        }
    }

    @Override
    public SpawnMode canSpawn(Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public boolean needRolesForBots()
    {
        return false;
    }

    @Override
    public Task getBotStartingTask(TaskStack taskStack, BotClient client)
    {
        return new TaskHunter(taskStack, client);
    }
}
