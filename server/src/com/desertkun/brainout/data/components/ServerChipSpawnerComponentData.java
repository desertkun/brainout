package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.ServerChipSpawnerComponent;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.ChipData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ServerChipSpawnerComponent")
@ReflectAlias("data.components.ServerChipSpawnerComponentData")
public class ServerChipSpawnerComponentData extends Component<ServerChipSpawnerComponent> implements WithTag
{
    private final ActiveData spawnerData;
    private float timer;
    private boolean enabled;

    private Team owner;
    private ServerChipReceiverComponentData deliveredTo;

    public ServerChipSpawnerComponentData(ActiveData spawnerData,
                                          ServerChipSpawnerComponent spawnerComponent)
    {
        super(spawnerData, spawnerComponent);

        this.spawnerData = spawnerData;
        this.timer = 5.0f;
        this.owner = null;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    private void spawn()
    {
        Array<ConsumableRecord> records = new Array<>();

        ChipData chip = getContentComponent().getChip().getData(spawnerData.getDimension());
        chip.setSkin(getContentComponent().getChip().getDefaultSkin());
        chip.getComponent(ServerChipComponentData.class).setSpawner(this);

        records.add(new ConsumableRecord(new InstrumentConsumableItem(chip, spawnerData.getDimension()), 1, 0));

        ServerMap.dropItem(spawnerData.getDimension(),
            getContentComponent().getDropItem(), records, -1, spawnerData.getX(), spawnerData.getY(), 0, 0);
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (!isEnabled())
            return;

        timer -= dt;
        if (timer < 0)
        {
            timer = 1.0f;

            checkChip();
        }
    }

    private void setOwner(Team owner)
    {
        this.owner = owner;
    }

    private boolean checkRecord(ConsumableRecord record)
    {
        if (record.getItem() instanceof InstrumentConsumableItem)
        {
            InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());
            InstrumentData instrumentData = ici.getInstrumentData();

            if (instrumentData instanceof ChipData)
            {
                ChipData chipData = ((ChipData) instrumentData);
                ServerChipComponentData s = chipData.getComponent(ServerChipComponentData.class);

                if (s != null && s.getSpawner() == this)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean find()
    {
        ServerMap map = getMap(ServerMap.class);

        if (map == null)
            return false;

        for (ActiveData chip: map.getActivesForTag(Constants.ActiveTags.CHIP, false))
        {
            if (chip instanceof ItemData)
            {
                ItemData itemData = ((ItemData) chip);

                for (ObjectMap.Entry<Integer, ConsumableRecord> entry : itemData.getRecords().getData())
                {
                    ConsumableRecord record = entry.value;

                    if (checkRecord(record))
                    {
                        setOwner(null);
                        return true;
                    }
                }
            }
        }

        for (ActiveData player: map.getActivesForTag(Constants.ActiveTags.PLAYERS, false))
        {
            PlayerOwnerComponent poc = player.getComponent(PlayerOwnerComponent.class);

            if (poc != null)
            {
                for (ObjectMap.Entry<Integer, ConsumableRecord> entry : poc.getConsumableContainer().getData())
                {
                    ConsumableRecord record = entry.value;

                    if (checkRecord(record))
                    {
                        // got one in players hands
                        setOwner(player.getTeam());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isDelivered()
    {
        return deliveredTo != null;
    }

    private void checkChip()
    {
        if (!find())
        {
            spawn();
        }
    }

    public Team getOwner()
    {
        return owner;
    }

    public ServerChipReceiverComponentData getDeliveredTo()
    {
        return deliveredTo;
    }

    public void setDeliveredTo(ServerChipReceiverComponentData deliveredTo)
    {
        this.deliveredTo = deliveredTo;
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.CHIP_SPAWNER);
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
