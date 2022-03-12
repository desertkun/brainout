package com.desertkun.brainout.events;

public class HitConfirmEvent extends Event
{
    public String collider;
    public int d, obj, dmg;
    public float x, y;

    @Override
    public ID getID()
    {
        return ID.hitConfirmed;
    }

    private Event init(String collider, int d, int obj, float x, float y, int dmg)
    {
        this.collider = collider;
        this.d = d;
        this.obj = obj;
        this.x = x;
        this.y = y;
        this.dmg = dmg;

        return this;
    }

    public static Event obtain(String collider, int d, int obj, float x, float y, int dmg)
    {
        HitConfirmEvent e = obtain(HitConfirmEvent.class);
        if (e == null) return null;
        return e.init(collider, d, obj, x, y, dmg);
    }

    @Override
    public void reset()
    {
        collider = null;
        this.d = -1;
        this.obj = -1;
        this.x = 0;
        this.y = 0;
        this.dmg = 0;
    }
}
