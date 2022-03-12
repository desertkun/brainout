package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;

public class ComponentUpdatedEvent extends Event
{
    public Component component;
    public ActiveData data;
    public Predicate predicate;

    public interface Predicate
    {
        boolean check(int owner);
    }

    @Override
    public ID getID()
    {
        return ID.componentUpdated;
    }

    private Event init(Component component, ActiveData data, Predicate predicate)
    {
        this.component = component;
        this.data = data;
        this.predicate = predicate;

        return this;
    }

    public static Event obtain(Component component, ActiveData data)
    {
        return obtain(component, data, null);
    }

    public static Event obtain(Component component, ActiveData data, Predicate predicate)
    {
        ComponentUpdatedEvent e = obtain(ComponentUpdatedEvent.class);
        if (e == null) return null;
        return e.init(component, data, predicate);
    }

    @Override
    public void reset()
    {
        this.component = null;
        this.data = null;
        this.predicate = null;
    }
}
