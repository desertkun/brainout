package com.desertkun.brainout.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.content.consumable.ConsumableContent;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.content.consumable.impl.InstrumentConsumableItem;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.content.shop.Slot;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableContainer;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.HookInstrumentEvent;
import com.desertkun.brainout.events.ResetInstrumentEvent;
import com.desertkun.brainout.events.SetInstrumentEvent;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("poc")
@ReflectAlias("content.components.PlayerOwnerComponent")
public class PlayerOwnerComponent extends Component implements Json.Serializable, WithTag
{
    private PlayerData playerData;
    private ConsumableContainer container;
    private ConsumableRecord currentInstrument;
    private ConsumableRecord hookedInstrument;
    private boolean enabled;

    public PlayerOwnerComponent(ComponentObject componentObject)
    {
        this((PlayerData)componentObject);
    }

    public PlayerOwnerComponent(PlayerData playerData)
    {
        super(playerData, null);

        this.playerData = playerData;
        this.container = new ConsumableContainer(playerData);
        this.currentInstrument = null;
        this.hookedInstrument = null;
        this.enabled = true;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;

        if (!this.enabled)
        {
            SimplePhysicsComponentData phy = getComponentObject().getComponentWithSubclass(SimplePhysicsComponentData.class);

            if (phy != null)
            {
                phy.getSpeed().set(0, 0);
            }
        }
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    public InstrumentData getCurrentInstrument()
    {
        if (currentInstrument == null)
        {
            return null;
        }

        if (currentInstrument.getItem() == null)
        {
            return null;
        }

        return ((InstrumentConsumableItem) currentInstrument.getItem()).getInstrumentData();
    }

    public ConsumableRecord getOptimalHookedInstrument()
    {
        for (ObjectMap.Entry<Integer, ConsumableRecord> entry : getConsumableContainer().getData())
        {
            ConsumableRecord record = entry.value;

            if (record == currentInstrument)
                continue;

            ConsumableItem item = record.getItem();

            if (!(item instanceof InstrumentConsumableItem))
                continue;

            InstrumentConsumableItem ici = ((InstrumentConsumableItem) item);
            InstrumentData instrumentData = ici.getInstrumentData();

            Slot slot = instrumentData.getInstrument().getSlot();
            if (slot == null)
                continue;

            if (!slot.getID().equals("slot-primary"))
                continue;

            return record;
        }

        return null;
    }

    public InstrumentData getHookedInstrument()
    {
        if (hookedInstrument == null)
        {
            return null;
        }

        if (hookedInstrument.getItem() == null)
        {
            return null;
        }

        return ((InstrumentConsumableItem) hookedInstrument.getItem()).getInstrumentData();
    }

    public ConsumableRecord getCurrentInstrumentRecord()
    {
        return currentInstrument;
    }

    public ConsumableRecord getHookedInstrumentRecord()
    {
        return hookedInstrument;
    }

    public void updateInstrument()
    {
        playerData.setCurrentInstrument(getCurrentInstrument());
    }

    public void updateHookedInstrument()
    {
        playerData.setHookedInstrument(getHookedInstrument());
    }

    public void setCurrentInstrument(ConsumableRecord record)
    {
        if (record != null && !(record.getItem() instanceof InstrumentConsumableItem))
        {
            return;
        }

        currentInstrument = record;

        selectInstrument();
        updateInstrument();

        ConsumableRecord hooked = getOptimalHookedInstrument();

        if (hooked != hookedInstrument)
        {
            setHookedInstrument(hooked);
        }
    }

    public void setHookedInstrument(ConsumableRecord record)
    {
        if (record != null && !(record.getItem() instanceof InstrumentConsumableItem))
        {
            return;
        }

        if (hookedInstrument != null)
        {
            unselectHookedInstrument();
        }

        hookedInstrument = record;

        selectHookedInstrument();
        updateHookedInstrument();
    }

    public ConsumableRecord setCurrentInstrument(int id)
    {
        // additional case for -1
       if (id == -1)
       {
           setCurrentInstrument(null);
           return null;
       }

        ConsumableRecord record = container.get(id);

        if (record == null)
        {
            return null;
        }

        ConsumableItem item = record.getItem();

        if (item == null || !(item instanceof InstrumentConsumableItem))
        {
            return null;
        }

        setCurrentInstrument(record);

        return record;
    }

    private void selectInstrument()
    {
        if (getCurrentInstrument() != null)
        {
            BrainOut.EventMgr.sendDelayedEvent(playerData, SetInstrumentEvent.obtain(getCurrentInstrument(), playerData));
        }
        else
        {
            BrainOut.EventMgr.sendDelayedEvent(playerData, ResetInstrumentEvent.obtain(playerData));
        }
    }

    private void selectHookedInstrument()
    {
        InstrumentData hooked = getHookedInstrument();

        if (hooked != null)
        {
            BrainOut.EventMgr.sendDelayedEvent(hooked,
                HookInstrumentEvent.obtain(hooked, playerData));
        }
    }

    private void unselectHookedInstrument()
    {
        InstrumentData hooked = getHookedInstrument();

        if (hooked != null)
        {
            BrainOut.EventMgr.sendDelayedEvent(hooked,
                HookInstrumentEvent.obtain(null, playerData));
        }
    }

    public ConsumableContainer.AcquiredConsumables getAmmo(int clipSize, Bullet bullet)
    {
        return container.getConsumable(clipSize, bullet);
    }

    public void putAmmo(int amount, Bullet bullet, int quality)
    {
        if (bullet != null)
        {
            container.putConsumable(amount, bullet.acquireConsumableItem(), quality);
        }
    }

    public boolean hasAmmo(Bullet bullet)
    {
        return container.hasConsumable(bullet);
    }

    @Override
    public void init()
    {
        super.init();

        getConsumableContainer().init();

        if (getCurrentInstrument() != null)
        {
            selectInstrument();
        }
    }

    public ConsumableContainer getConsumableContainer()
    {
        return container;
    }

    public ConsumableRecord getInstrumentForSlot(Slot slot)
    {
        Array<ConsumableRecord> records = getConsumableContainer().queryRecords(
                record -> record.getItem() instanceof InstrumentConsumableItem
        );

        for (ConsumableRecord record: records)
        {
            if (record.getItem() instanceof InstrumentConsumableItem)
            {
                InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());

                if (((Instrument) ici.getContent()).getSlot() == slot)
                {
                    return record;
                }
            }
        }

        return null;
    }

    public ConsumableRecord getNextInstrumentForSlot(Slot slot, ConsumableRecord old)
    {
        Array<ConsumableRecord> records = getConsumableContainer().queryRecords(
            record -> {
                if (!(record.getItem() instanceof InstrumentConsumableItem))
                    return false;

                InstrumentConsumableItem ici = ((InstrumentConsumableItem) record.getItem());

                if (((Instrument) ici.getContent()).getSlot() != slot)
                    return false;

                return true;
            }
        );

        if (records.size == 0)
            return null;

        int index = records.indexOf(old, true);

        if (index < 0)
            return records.get(0);

        index++;
        if (index > records.size - 1)
            index = 0;

        return records.get(index);
    }

    public ConsumableContent switchConsumable(Class classOf)
    {
        return switchConsumable(null, classOf);
    }

    @SuppressWarnings("unchecked")
    public ConsumableContent switchConsumable(ConsumableContent old, Class classOf)
    {
        int index;

        if (old != null)
        {
            index = container.getIndex(old) + 1;
        }
        else
        {
            index = 0;
        }

        for (int i = index; i < container.size(); i++)
        {
            ConsumableRecord at = container.getByIndex(i);

            if (BrainOut.R.instanceOf(classOf, at.getItem().getContent()))
            {
                return ((ConsumableContent) at.getItem().getContent());
            }
        }

        // if nothing found, start from begin
        for (int i = 0; i < container.size(); i++)
        {
            ConsumableRecord at = container.getByIndex(i);

            if (BrainOut.R.instanceOf(classOf, at.getItem().getContent()))
            {
                return ((ConsumableContent) at.getItem().getContent());
            }
        }

        return null;
    }

    @Override
    public void release()
    {
        super.release();

        container.release();
    }

    @Override
    public void write(Json json)
    {
        json.writeValue("container", container);

        if (currentInstrument != null)
        {
            json.writeValue("current", currentInstrument.getId());
        }

        if (hookedInstrument != null)
        {
            json.writeValue("hooked", hookedInstrument.getId());
        }

        json.writeValue("en", enabled);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        clear();

        enabled = jsonData.getBoolean("en", true);

        if (jsonData.has("container"))
        {
            container.read(json, jsonData.get("container"));
        }

        if (jsonData.has("current"))
        {
            currentInstrument = getConsumableContainer().get(jsonData.getInt("current"));
        }
        else
        {
            currentInstrument = null;
        }

        if (jsonData.has("hooked"))
        {
            hookedInstrument = getConsumableContainer().get(jsonData.getInt("hooked"));
        }
        else
        {
            hookedInstrument = null;
        }
    }

    private void clear()
    {
        currentInstrument = null;
        hookedInstrument = null;
    }

    public void removeConsumable(ConsumableRecord consumableRecord)
    {
        if (consumableRecord == currentInstrument)
        {
            setCurrentInstrument(null);
        }

        if (consumableRecord == hookedInstrument)
        {
            setHookedInstrument(null);
        }

        container.removeRecord(consumableRecord);
    }

    public void removeConsumable(int id)
    {
        ConsumableRecord consumableRecord = container.get(id);

        if (consumableRecord == null)
        {
            return;
        }

        removeConsumable(consumableRecord);
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

    public InstrumentData getInstrument(int recordId)
    {
        ConsumableRecord consumableRecord = container.get(recordId);

        if (consumableRecord == null)
        {
            return null;
        }

        if (consumableRecord.getItem() instanceof InstrumentConsumableItem)
        {
            return ((InstrumentConsumableItem) consumableRecord.getItem()).getInstrumentData();
        }

        return null;
    }

    public ConsumableRecord findRecord(InstrumentData instrumentData)
    {
        for (int i = 0; i < container.size(); i++)
        {
            ConsumableRecord at = container.getByIndex(i);

            if (at.getItem() instanceof InstrumentConsumableItem)
            {
                if (((InstrumentConsumableItem) at.getItem()).getInstrumentData() == instrumentData)
                {
                    return at;
                }
            }
        }

        return null;
    }

    @Override
    public int getLayer()
    {
        return 0;
    }

    public void setPlayerData(PlayerData playerData)
    {
        this.playerData = playerData;
    }

    @Override
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.RESOURCE_RECEIVER);
    }
}
