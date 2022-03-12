package com.desertkun.brainout.events;

import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.data.components.base.ComponentObject;

public class DamagedEvent extends Event
{
    public ComponentObject data;
    public float health;
    public float x, y, angle;
    public Content content;
    public String damageKind;

    public DamagedEvent()
    {
    }

    @Override
    public ID getID()
    {
        return ID.damaged;
    }

    public Event init(ComponentObject data,
                       float health, float x, float y, float angle, Content content, String damageKind)
    {
        this.data = data;
        this.health = health;

        this.x = x;
        this.y = y;
        this.angle = angle;

        this.content = content;
        this.damageKind = damageKind;

        return this;
    }

    public static Event obtain(ComponentObject data,
                               float health, float x, float y, float angle, Content content, String damageKind)
    {
        DamagedEvent e = obtain(DamagedEvent.class);
        if (e == null) return null;
        return e.init(data, health, x, y, angle, content, damageKind);
    }

    @Override
    public void reset()
    {
        this.data = null;
        this.health = 0;

        this.x = 0;
        this.y = 0;
        this.angle = 0;

        this.content = null;
        this.damageKind = null;
    }
}
