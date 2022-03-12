package com.desertkun.brainout.components;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.instrument.Weapon;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.BotControllerComponentData;
import com.desertkun.brainout.data.components.SecondaryWeaponSlotComponentData;
import com.desertkun.brainout.data.components.WeaponAnimationComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.LaunchEffectEvent;

public class ServerBotWeaponComponent extends Component
{
    private final ConsumableRecord record;
    private final WeaponData data;
    private WeaponSlotComponent currentSlot;
    private ObjectMap<String, WeaponSlotComponent> slots;

    public ServerBotWeaponComponent(WeaponData weaponData, ConsumableRecord record)
    {
        super(weaponData, null);

        this.data = weaponData;
        this.record = record;
        this.slots = new ObjectMap<>();
    }

    public WeaponSlotComponent getCurrentSlot()
    {
        return currentSlot;
    }

    public WeaponSlotComponent getSlot(String slot)
    {
        if (slots.containsKey(slot))
        {
            return slots.get(slot);
        }

        return null;
    }

    public WeaponSlotComponent addSlot(Weapon.WeaponProperties properties, String name)
    {
        WeaponSlotComponent slot = new ServerBotWeaponSlotComponent(data, record, properties, name, this::getSlot);

        slots.put(name, slot);
        slot.init();

        return slot;
    }

    @Override
    public void init()
    {
        super.init();

        WeaponSlotComponent slot = addSlot(data.getWeapon().getPrimaryProperties(),
            Constants.Properties.SLOT_PRIMARY);

        setCurrentSlot(slot);

        SecondaryWeaponSlotComponentData secondary =
                data.getComponent(SecondaryWeaponSlotComponentData.class);

        if (secondary != null)
        {
            addSlot(secondary.getWeaponProperties(), Constants.Properties.SLOT_SECONDARY);
        }
    }

    public boolean setCurrentSlot(WeaponSlotComponent currentSlot)
    {
        if (this.currentSlot != currentSlot)
        {
            this.currentSlot = currentSlot;
            return true;
        }

        return false;
    }

    private void deselected()
    {
        WeaponSlotComponent slot = getCurrentSlot();

        if (slot == null)
            return;

        if (slot.getState() == WeaponSlotComponent.State.cocked ||
            slot.getState() == WeaponSlotComponent.State.cocking)
        {
            slot.doReset(false);
        }
    }

    public WeaponSlotComponent switchWeaponSlot(String name)
    {
        if (name == null)
        {
            name = Constants.Properties.SLOT_PRIMARY;
        }

        if (slots.containsKey(name))
        {
            WeaponSlotComponent slot = getSlot(name);
            if (setCurrentSlot(slot))
            {
                WeaponAnimationComponentData cwcd = data.getComponent(WeaponAnimationComponentData.class);

                if (cwcd != null)
                {
                    BrainOut.EventMgr.sendDelayedEvent(data,
                            LaunchEffectEvent.obtain(LaunchEffectEvent.Kind.switchMode, cwcd.getLaunchPointData()));
                }
            }
            return slot;
        }

        return null;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        getCurrentSlot().update(dt);
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    public ConsumableRecord getRecord()
    {
        return record;
    }

    @Override
    public boolean onEvent(Event event)
    {

        switch (event.getID()) {
            case ownerChanged: {
                InstrumentData instrument = (InstrumentData)getComponentObject();
                ActiveData owner = instrument.getOwner();
                if (owner != null && !(owner instanceof PlayerData)) {
                    BotControllerComponentData botController = owner.getComponent(BotControllerComponentData.class);
                    if (botController == null)
                        getComponentObject().removeComponent(this);
                }
                break;
            }
        }

        return false;
    }
}
