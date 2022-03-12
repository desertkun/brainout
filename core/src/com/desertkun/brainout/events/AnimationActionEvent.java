package com.desertkun.brainout.events;

public class AnimationActionEvent extends Event
{
    public String kind;
    public String payload;

    @Override
    public ID getID()
    {
        return ID.animationAction;
    }

    private Event init(String kind, String payload)
    {
        this.kind = kind;
        this.payload = payload;

        return this;
    }

    public static Event obtain(String kind, String payload)
    {
        AnimationActionEvent e = obtain(AnimationActionEvent.class);
        if (e == null) return null;
        return e.init(kind, payload);
    }

    @Override
    public void reset()
    {
        kind = null;
        payload = null;
    }
}
