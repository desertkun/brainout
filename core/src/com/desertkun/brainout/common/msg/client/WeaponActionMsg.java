package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.content.bullet.Bullet;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class WeaponActionMsg
{
    public enum Action
    {
        load,
        loadBoth,
        unload,
        cock,
        buildUp,
        fetch
    }

    public int recordId;
    public Action action;
    public String slot;
    public String slotB;

    public WeaponActionMsg() {}
    public WeaponActionMsg(ConsumableRecord record, Action action, String slot)
    {
        this(record, action, slot, null);
    }

    public WeaponActionMsg(ConsumableRecord record, Action action, String slot, String slotB)
    {
        this.recordId = record.getId();
        this.action = action;
        this.slot = slot;
        this.slotB = slotB;
    }
}
