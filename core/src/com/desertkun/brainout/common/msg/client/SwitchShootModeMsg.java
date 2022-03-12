package com.desertkun.brainout.common.msg.client;

import com.desertkun.brainout.content.instrument.Weapon;

public class SwitchShootModeMsg
{
    public Weapon.ShootMode sm;
    public String slot;

    public SwitchShootModeMsg() {}
    public SwitchShootModeMsg(Weapon.ShootMode sm, String slot)
    {
        this.sm = sm;
        this.slot = slot;
    }
}
