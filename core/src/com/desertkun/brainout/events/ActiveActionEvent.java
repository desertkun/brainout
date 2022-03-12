package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.events.Event;

public class ActiveActionEvent extends Event
{
    public ActiveData activeData;
    public Action action;
    public ActiveData.ComponentWriter componentWriter;
    public boolean flag;

    @Override
    public void reset()
    {
        activeData = null;
        action = null;
        componentWriter = null;
    }

    public enum Action
    {
        added,
        removed,
        updated
    }

    private Event init(ActiveData activeData, Action action)
    {
        return init(activeData, action, ActiveData.ComponentWriter.TRUE, true);
    }

    private Event init(ActiveData activeData, Action action, boolean flag)
    {
        return init(activeData, action, ActiveData.ComponentWriter.TRUE, flag);
    }

    private Event init(ActiveData activeData, Action action, ActiveData.ComponentWriter componentWriter, boolean flag)
    {
        this.activeData = activeData;
        this.action = action;
        this.componentWriter = componentWriter;
        this.flag = flag;

        return this;
    }

    public static Event obtain(ActiveData activeData, Action action, boolean flag)
    {
        ActiveActionEvent e = obtain(ActiveActionEvent.class);
        if (e == null) return null;
        return e.init(activeData, action, flag);
    }

    public static Event obtain(ActiveData activeData, Action action)
    {
        ActiveActionEvent e = obtain(ActiveActionEvent.class);
        if (e == null) return null;
        return e.init(activeData, action);
    }

    public static Event obtain(ActiveData activeData, Action action, ActiveData.ComponentWriter componentWriter)
    {
        ActiveActionEvent e = obtain(ActiveActionEvent.class);
        if (e == null) return null;
        return e.init(activeData, action, componentWriter, true);
    }

    @Override
    public ID getID()
    {
        return ID.activeAction;
    }
}
