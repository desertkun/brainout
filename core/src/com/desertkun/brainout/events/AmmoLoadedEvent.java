package com.desertkun.brainout.events;

public class AmmoLoadedEvent extends Event
{

    public int weaponId, magazineId, bulletsId, ammoCount;

    public AmmoLoadedEvent()
    {
        this.weaponId = -1;
        this.magazineId = -1;
        this.bulletsId = -1;
        this.ammoCount = 0;
    }

    @Override
    public ID getID()
    {
        return ID.ammoLoaded;
    }

    private Event init(int weaponId, int magazineId, int bulletsId, int ammoCount)
    {
        this.weaponId = weaponId;
        this.magazineId = magazineId;
        this.bulletsId = bulletsId;
        this.ammoCount = ammoCount;

        return this;
    }

    public static Event obtain(int weaponId, int magazineId, int bulletsId, int ammoCount)
    {
        AmmoLoadedEvent e = obtain(AmmoLoadedEvent.class);
        if (e == null) return null;
        return e.init(weaponId, magazineId, bulletsId, ammoCount);
    }

    @Override
    public void reset()
    {
        this.weaponId = -1;
        this.magazineId = -1;
        this.bulletsId = -1;
        this.ammoCount = 0;
    }
}
