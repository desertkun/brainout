package com.desertkun.brainout.events;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.desertkun.brainout.data.active.ActiveData;

public class PhysicsContactEvent extends Event
{
    public Vector2 speed;
    public ActiveData activeData;

    public PhysicsContactEvent()
    {
        this.speed = new Vector2();
    }

    @Override
    public ID getID()
    {
        return ID.physicsContact;
    }

    private Event init(Vector2 speed, ActiveData activeData)
    {
        this.speed.set(speed);
        this.activeData = activeData;

        return this;
    }

    public static Event obtain(Vector2 speed, ActiveData activeData)
    {
        PhysicsContactEvent e = obtain(PhysicsContactEvent.class);
        if (e == null) return null;
        return e.init(speed, activeData);
    }

    @Override
    public void reset()
    {
        this.speed.set(0, 0);
        this.activeData = null;
    }
}
