package com.desertkun.brainout.events;

import com.desertkun.brainout.components.WeaponSlotComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;

public class WeaponStateUpdatedEvent extends Event
{
    public ActiveData playerData;
    public WeaponData weaponData;
    public WeaponSlotComponent slot;
    public WeaponSlotComponent.State state;

    @Override
    public ID getID()
    {
        return ID.weaponStateUpdated;
    }

    private Event init(ActiveData playerData, WeaponData weaponData, WeaponSlotComponent slot, WeaponSlotComponent.State state)
    {
        this.playerData = playerData;
        this.weaponData = weaponData;
        this.slot = slot;
        this.state = state;

        return this;
    }

    public static Event obtain(ActiveData playerData, WeaponData weaponData, WeaponSlotComponent slot, WeaponSlotComponent.State state)
    {
        WeaponStateUpdatedEvent e = obtain(WeaponStateUpdatedEvent.class);
        if (e == null) return null;
        return e.init(playerData, weaponData, slot, state);
    }

    @Override
    public void reset()
    {
        this.playerData = null;
        this.weaponData = null;
        this.slot = null;
        this.state = null;
    }
}
