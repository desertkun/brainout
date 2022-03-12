package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.common.enums.NotifyAward;
import com.desertkun.brainout.common.enums.NotifyMethod;
import com.desertkun.brainout.common.enums.NotifyReason;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.ServerChipItemComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.ChipData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.WithTag;

import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerChipItemComponent")
@ReflectAlias("data.components.ServerChipItemComponentData")
public class ServerChipItemComponentData extends ServerItemComponentData<ServerChipItemComponent> implements WithTag
{
    private final boolean atSpawn;
    private final ItemData itemData;
    private ServerChipReceiverComponentData delivered;

    public ServerChipItemComponentData(ItemData itemData,
                                       ServerChipItemComponent itemComponent)
    {
        super(itemData, itemComponent);

        atSpawn = itemComponent.isAtSpawnPoint();
        this.itemData = itemData;
    }

    @Override
    protected boolean earn(Client client)
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (gameMode == null)
            return false;

        if (!gameMode.isGameActive(true, true))
            return false;

        return stolen(client) && super.earn(client);
    }

    private ServerChipComponentData checkRecord(ConsumableRecord record)
    {
        if (record.getItem() instanceof InstrumentConsumableItem)
        {
            InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
            InstrumentData instrumentData = ici.getInstrumentData();

            if (instrumentData instanceof ChipData)
            {
                ChipData chipData = ((ChipData) instrumentData);
                ServerChipComponentData s = chipData.getComponent(ServerChipComponentData.class);

                return s;
            }
        }

        return null;
    }


    public ServerChipComponentData getChip()
    {
        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : itemData.getRecords().getData())
        {
            ConsumableRecord record = entry.value;

            ServerChipComponentData chip = checkRecord(record);
            if (chip != null)
            {
                return chip;
            }
        }

        return null;
    }

    private boolean stolen(Client playerTook)
    {
        Team takingTeam = playerTook.getTeam();

        boolean isDelivered = isDelivered();

        if (isDelivered)
        {
            if (delivered.getTeam() == takingTeam)
            {
                // friends can't take chip from own base

                return false;
            }
        }

        if (atSpawn || isDelivered)
        {
            // notify everyone
            for (ObjectMap.Entry<Integer, Client> clientEntry : BrainOutServer.Controller.getClients())
            {
                Client client = clientEntry.value;

                if (client.getTeam() == takingTeam)
                {
                    client.notify(NotifyAward.score, 0, NotifyReason.chipWeStolen, NotifyMethod.message, null);
                } else
                {
                    client.notify(NotifyAward.score, 0, NotifyReason.chipEnemyStolen, NotifyMethod.message, null);
                }
            }
        }

        // it is stolen
        setDelivered(null);

        return true;
    }

    public void setDelivered(ServerChipReceiverComponentData delivered)
    {
        this.delivered = delivered;
    }

    public boolean isDelivered()
    {
        return delivered != null;
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.CHIP);
    }
}
