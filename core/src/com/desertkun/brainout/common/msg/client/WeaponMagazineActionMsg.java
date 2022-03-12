package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class WeaponMagazineActionMsg
{
    public enum Action
    {
        loadOne,
        unloadAll
    }

    public int recordId;
    public int magazineId;
    public Action action;
    public String slot;

    public WeaponMagazineActionMsg() {}
    public WeaponMagazineActionMsg(ConsumableRecord record, Action action, String slot, int magazineId)
    {
        this.recordId = record.getId();
        this.action = action;
        this.slot = slot;
        this.magazineId = magazineId;
    }
}
