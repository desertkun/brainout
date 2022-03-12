package com.desertkun.brainout.events;

import com.badlogic.gdx.utils.Align;

public class OnScreenMessagesEvent extends Event
{
    public String message;
    public float time;
    public boolean doNotForce;
    public int align;
    public String style;
    public String name;
    public boolean isTimer;

    @Override
    public ID getID()
    {
        return ID.onScreenMessage;
    }

    private Event init(String message, float time, boolean doNotForce, int align, String style, String name, boolean isTimer)
    {
        this.message = message;
        this.time = time;
        this.doNotForce = doNotForce;
        this.align = align;
        this.style = style;
        this.name = name;
        this.isTimer = isTimer;

        return this;
    }

    public static Event obtain(String message, float time, boolean doNotForce, int align, String style, String name, boolean isTimer)
    {
        OnScreenMessagesEvent e = obtain(OnScreenMessagesEvent.class);
        if (e == null) return null;
        return e.init(message, time, doNotForce, align, style, name, isTimer);
    }

    public static Event obtain(String message, float time, boolean doNotForce)
    {
        OnScreenMessagesEvent e = obtain(OnScreenMessagesEvent.class);
        if (e == null) return null;
        return e.init(message, time, doNotForce, Align.center, null, null, false);
    }

    @Override
    public void reset()
    {
        this.message = null;
        this.time = 0;
        this.doNotForce = false;
    }
}
