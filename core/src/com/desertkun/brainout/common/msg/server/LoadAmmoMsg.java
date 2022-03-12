package com.desertkun.brainout.common.msg.server;

import com.desertkun.brainout.common.msg.UdpMessage;

public class LoadAmmoMsg implements UdpMessage
{
    public int weaponId, magazineId, bulletsId, ammoCount;

    public LoadAmmoMsg() {}

    public LoadAmmoMsg(int weaponId, int magazineId, int bulletsId, int ammoCount)
    {
        this.weaponId = weaponId;
        this.magazineId = magazineId;
        this.bulletsId = bulletsId;
        this.ammoCount = ammoCount;
    }
}
