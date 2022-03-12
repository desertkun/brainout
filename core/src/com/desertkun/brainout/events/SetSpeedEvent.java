package com.desertkun.brainout.events;

import com.badlogic.gdx.math.Vector2;

public class SetSpeedEvent extends Event
{
    public Vector2 speed;
    public Vector2 coef;

    public SetSpeedEvent()
    {
        this.speed = new Vector2();
        this.coef = new Vector2();
    }

    @Override
    public ID getID()
    {
        return ID.setSpeed;
    }

    private Event init(float x, float y, float cx, float cy)
    {
        this.speed.set(x, y);
        this.coef.set(cx, cy);

        return this;
    }

    public static Event obtain(float x, float y, float cx, float cy)
    {
        SetSpeedEvent e = obtain(SetSpeedEvent.class);
        if (e == null) return null;
        return e.init(x, y, cx, cy);
    }

    @Override
    public void reset()
    {
        this.speed.set(0, 0);
        this.coef.set(0, 0);
    }
}
