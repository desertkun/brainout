package com.desertkun.brainout.events;

public class ItemActionEvent extends Event
{
    public String action;

    @Override
    public ID getID()
    {
        return ID.itemAction;
    }

    private Event init(String action)
    {
        this.action = action;

        return this;
    }

    public static Event obtain(String action)
    {
        ItemActionEvent e = obtain(ItemActionEvent.class);
        if (e == null) return null;
        return e.init(action);
    }

    @Override
    public void reset()
    {
        this.action = null;
    }
}
