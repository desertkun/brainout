package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.ServerChipReceiverComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.ChipData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.DetectedEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerChipReceiverComponent")
@ReflectAlias("data.components.ServerChipReceiverComponentData")
public class ServerChipReceiverComponentData extends Component<ServerChipReceiverComponent> implements WithTag
{
    private final ActiveData activeData;
    private ObjectSet<ServerChipSpawnerComponentData> chips;
    private ObjectSet<ServerChipSpawnerComponentData> history;

    public ServerChipReceiverComponentData(ActiveData activeData,
                                           ServerChipReceiverComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;
        this.chips = new ObjectSet<>();
        this.history = new ObjectSet<>();
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case detected:
            {
                DetectedEvent detectedEvent = ((DetectedEvent) event);

                detected(detectedEvent.detected, detectedEvent.eventKind);

                break;
            }
        }

        return false;
    }


    private void detected(ActiveData activeData, DetectedEvent.EventKind eventKind)
    {
        if (activeData instanceof PlayerData)
        {
            PlayerData playerData = ((PlayerData) activeData);
            Client client = BrainOutServer.Controller.getClients().get(playerData.getOwnerId());

            if (client == null)
                return;

            if (!(client instanceof PlayerClient))
                return;

            PlayerClient playerClient = ((PlayerClient) client);

            PlayerOwnerComponent poc = playerData.getComponent(PlayerOwnerComponent.class);
            ConsumableContainer cnt = poc.getConsumableContainer();

            for (ObjectMap.Entry<Integer, ConsumableRecord> entry : cnt.getData())
            {
                ConsumableRecord record = entry.value;

                if (record.getItem() instanceof InstrumentConsumableItem)
                {
                    InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
                    InstrumentData instrumentData = ici.getInstrumentData();

                    if (instrumentData instanceof ChipData)
                    {
                        ChipData chipData = ((ChipData) instrumentData);

                        ServerChipComponentData data = chipData.getComponent(ServerChipComponentData.class);
                        if (data != null)
                        {
                            ServerChipSpawnerComponentData spawner = data.getSpawner();

                            switch (eventKind)
                            {
                                case enter:
                                {
                                    if (chipTaken(spawner, data.getChipData(), playerClient))
                                    {
                                        playerClient.getServerPlayerController().dropConsumable(
                                            record.getId(), playerData.getAngle(), 1);
                                    }

                                    break;
                                }
                                case leave:
                                {
                                    chipLost(spawner, data.getChipData(), playerClient);

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        else if (activeData instanceof ItemData)
        {
            ItemData itemData = ((ItemData) activeData);

            ServerChipItemComponentData chipItem = itemData.getComponent(ServerChipItemComponentData.class);

            if (chipItem == null)
                return;

            ServerChipComponentData data = chipItem.getChip();

            if (data == null)
                return;

            ServerChipSpawnerComponentData spawner = data.getSpawner();

            PlayerClient playerClient = null;
            Client client = BrainOutServer.Controller.getClients().get(itemData.getOwnerId());
            if (client instanceof PlayerClient)
            {
                playerClient = ((PlayerClient) client);
            }

            switch (eventKind)
            {
                case enter:
                {
                    chipItem.setDelivered(this);
                    chipTaken(spawner, data.getChipData(), playerClient);

                    break;
                }
                case leave:
                {
                    chipLost(spawner, data.getChipData(), playerClient);

                    break;
                }
            }
        }
    }

    private void chipLost(ServerChipSpawnerComponentData spawner, ChipData chipData, PlayerClient playerClient)
    {
        if (!chips.contains(spawner))
        {
            return;
        }

        chips.remove(spawner);
        spawner.setDeliveredTo(null);

        Team lostTeam = activeData.getTeam();

        // notify everyone
        for (ObjectMap.Entry<Integer, Client> clientEntry : BrainOutServer.Controller.getClients())
        {
            Client client = clientEntry.value;

            if (client.getTeam() == lostTeam)
            {
                client.notify(NotifyAward.score, 0, NotifyReason.chipEnemyStolen, NotifyMethod.message, null);
            }
            else
            {
                client.notify(NotifyAward.score, 0, NotifyReason.chipWeStolen, NotifyMethod.message, null);
            }
        }
    }

    private boolean chipTaken(ServerChipSpawnerComponentData spawner, ChipData chipData, PlayerClient playerClient)
    {
        if (chips.contains(spawner))
        {
            return false;
        }

        chips.add(spawner);
        history.add(spawner);
        spawner.setDeliveredTo(this);

        if (playerClient == null)
            return true;

        Team takingTeam = playerClient.getTeam();
        float a = BrainOutServer.getInstance().getSettings().getPrice("chip-take");

        // notify everyone
        for (ObjectMap.Entry<Integer, Client> clientEntry : BrainOutServer.Controller.getClients())
        {
            Client client = clientEntry.value;

            if (client.getTeam() == takingTeam)
            {
                boolean shouldAward = client == playerClient && !history.contains(spawner);
                if (shouldAward)
                {
                    client.addStat("chips-taken", 1);
                    // award the players in zone
                    client.award(NotifyAward.score, a);
                }

                client.notify(NotifyAward.score,
                        shouldAward ? a : 0.0f,
                        NotifyReason.chipTaken, NotifyMethod.message, null);
            }
            else
            {
                client.notify(NotifyAward.score, 0, NotifyReason.chipLost, NotifyMethod.message, null);
            }
        }

        return true;
    }

    public Team getTeam()
    {
        return activeData.getTeam();
    }

    public ObjectSet<ServerChipSpawnerComponentData> getChips()
    {
        return chips;
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.CHIP_RECEIVER);
    }
}
