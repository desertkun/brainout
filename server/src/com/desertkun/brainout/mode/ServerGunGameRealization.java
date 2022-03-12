package com.desertkun.brainout.mode;

import com.badlogic.gdx.utils.*;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskHunter;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.common.enums.data.RankND;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.shop.ConsumableSlotItem;
import com.desertkun.brainout.content.shop.InstrumentSlotItem;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.content.upgrades.Upgrade;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ServerPlayerControllerComponentData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.InstrumentInfo;
import com.desertkun.brainout.mode.payload.GunGamePayload;
import com.desertkun.brainout.mode.payload.ModePayload;
import com.desertkun.brainout.playstate.PlayStateEndGame;
import com.desertkun.brainout.playstate.ServerPSEndGame;

import java.util.Comparator;
import java.util.TimerTask;

public class ServerGunGameRealization extends ServerRealization<GameModeGunGame<ServerGunGameRealization>>
{
    private boolean complete;
    private int playerWon;

    public ServerGunGameRealization(GameModeGunGame<ServerGunGameRealization> gameMode)
    {
        super(gameMode);

        complete = false;
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

    private int getClientRank(Client client)
    {
        ModePayload payload = client.getModePayload();

        if (!(payload instanceof GunGamePayload))
            return 0;

        return - ((GunGamePayload) payload).getRank();
    }

    @Override
    public boolean timedOut(PlayStateEndGame.GameResult gameResult)
    {
        Array<Client> clients = BrainOutServer.Controller.getClients().values().toArray();
        clients.sort(Comparator.comparingInt(this::getClientRank));

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
        if (!complete)
            return false;

        Array<Client> clients = BrainOutServer.Controller.getClients().values().toArray();
        clients.sort((o1, o2) -> o1.getScore() < o2.getScore() ? 1 : -1);

        if (clients.size > 0)
        {
            gameResult.setPlayerWon(playerWon);
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
    public SpawnMode acquireSpawn(Client client, Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
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
    public void clientCompletelyInitialized(Client client)
    {
        super.clientCompletelyInitialized(client);

        if (!client.isAlive())
        {
            client.requestSpawn();
        }

        updated();
    }

    @Override
    public boolean forceWeaponAutoLoad()
    {
        return true;
    }

    @Override
    public void clientReleased(Client client)
    {
        super.clientReleased(client);

        updated();
    }

    private int getMinimumRank()
    {
        Array<Client> clients = BrainOutServer.Controller.getClients().values().toArray();

        int minimum = 0;

        for (Client client : clients)
        {
            if (!client.isInitialized())
                continue;

            ModePayload payload = client.getModePayload();

            if (!(payload instanceof GunGamePayload))
                continue;

            GunGamePayload gunGamePayload = ((GunGamePayload) payload);

            if (minimum == 0 || minimum > gunGamePayload.getRank())
            {
                minimum = gunGamePayload.getRank();
            }
        }

        return minimum;
    }

    @Override
    public ModePayload newPlayerPayload(Client playerClient)
    {
        return new GunGamePayload(playerClient, getMinimumRank());
    }

    @Override
    public void write(Json json, int owner)
    {
        super.write(json, owner);

        json.writeObjectStart("ranks");

        for (ObjectMap.Entry<Integer, Client> entry : BrainOutServer.Controller.getClients())
        {
            Client playerClient = entry.value;
            ModePayload modePayload = playerClient.getModePayload();

            if (!(modePayload instanceof GunGamePayload))
                continue;

            int rank = ((GunGamePayload) modePayload).getRank();

            json.writeValue(entry.key.toString(), rank);
        }

        json.writeObjectEnd();
    }

    private void giveRankWeapon(Client playerClient, boolean init)
    {
        PlayerData playerData = playerClient.getPlayerData();

        if (playerData == null)
            return;

        if (!(playerClient.getModePayload() instanceof GunGamePayload))
        {
            playerClient.setModePayload(new GunGamePayload(playerClient, 0));
        }

        GunGamePayload gunGamePayload = ((GunGamePayload) playerClient.getModePayload());
        int rank = gunGamePayload.getRank();

        if (rank >= getGameMode().getGunGameWeapons().size)
            return;

        String weapon = getGameMode().getGunGameWeapons().get(rank);
        InstrumentSlotItem weaponToGo = BrainOutServer.ContentMgr.get(weapon, InstrumentSlotItem.class);
        if (weaponToGo == null)
            return;

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

        Instrument instrument = weaponToGo.getInstrument();

        InstrumentData instrumentData = instrument.getData(playerData.getDimension());
        instrumentData.setSkin(instrument.getDefaultSkin());

        for (ObjectMap.Entry<String, Array<Upgrade>> entry : weaponToGo.getUpgrades())
        {
            String key = entry.key;

            if (!"scope".equals(key))
                continue;

            // only scopes
            Array<Upgrade> upgrades = entry.value;

            if (upgrades.size == 0)
                continue;

            // put last scope if it is
            instrumentData.getUpgrades().put(key, upgrades.peek());
        }

        InstrumentConsumableItem ici = new InstrumentConsumableItem(instrumentData, playerData.getDimension());
        ConsumableRecord record = poc.getConsumableContainer().putConsumable(1, ici);

        for (ConsumableSlotItem.ItemCargo cargo : weaponToGo.getCargo())
        {
            Content content = cargo.getContent();

            if (!(content instanceof ConsumableContent))
                continue;

            poc.getConsumableContainer().putConsumable(cargo.getAmount(), ((ConsumableContent) content).acquireConsumableItem());
        }

        if (init)
        {
            instrumentData.init();

            ServerPlayerControllerComponentData ctl =
                playerData.getComponentWithSubclass(ServerPlayerControllerComponentData.class);

            playerData.setCurrentInstrument(instrumentData);
            ctl.changeInstrument(record.getId());
            ctl.updateAttachments();
            ctl.consumablesUpdated();
            ctl.instrumentSelected(record);
        }
    }

    @Override
    public void onClientSpawn(Client client, PlayerData player)
    {
        super.onClientSpawn(client, player);

        if (!getGameMode().isGameActive(true, true))
            return;

        giveRankWeapon(client, false);
    }

    @Override
    public void onClientDeath(Client playerClient, Client playerKiller, PlayerData playerData, InstrumentInfo info)
    {
        super.onClientDeath(playerClient, playerKiller, playerData, info);

        if (!getGameMode().isGameActive(false, false))
            return;

        if (playerClient == null || playerKiller == null)
            return;

        boolean knife = info != null && info.instrument != null &&
                "slot-melee".equals(info.instrument.getSlot().getID());

        if (playerKiller != playerClient)
        {
            if (knife)
            {
                // knifed
                ModePayload modePayload = playerClient.getModePayload();

                if (modePayload instanceof GunGamePayload)
                {
                    GunGamePayload gg = ((GunGamePayload) modePayload);

                    if (gg.demote())
                    {
                        giveRankWeapon(playerClient, true);
                    }
                }
            }

            ModePayload modePayload = playerKiller.getModePayload();

            if (modePayload instanceof GunGamePayload)
            {
                GunGamePayload gg = ((GunGamePayload) modePayload);

                if (gg.getRank() == getGameMode().getGunGameWeapons().size - 1)
                {
                    playerWon = playerKiller.getId();
                    complete = true;
                }
                else
                {
                    if (gg.promote(knife))
                    {
                        giveRankWeapon(playerKiller, true);

                        playerKiller.addStat("gungame-levelup", 1);
                        if (gg.getRank() == 15)
                        {
                            playerKiller.addStat("gungame-levelup-15", 1);
                        }

                        playerKiller.notify(NotifyAward.rankUp, 0, NotifyReason.gunGameLevelUpgrade, NotifyMethod.message,
                            new RankND(gg.getRank(), getGameMode().getGunGameWeapons().size - 1), true);
                    }
                }
            }
        }

        updated();
    }

    @Override
    public SpawnMode canSpawn(Team team)
    {
        return SpawnMode.allowed;
    }

    @Override
    public Task getBotStartingTask(TaskStack taskStack, BotClient client)
    {
        return new TaskHunter(taskStack, client);
    }

    @Override
    public boolean needRolesForBots()
    {
        return false;
    }
}
