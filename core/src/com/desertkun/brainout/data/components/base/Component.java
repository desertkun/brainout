package com.desertkun.brainout.data.components.base;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;
import com.desertkun.brainout.events.ComponentUpdatedEvent;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.inspection.Inspectable;
import com.desertkun.brainout.inspection.props.PropertiesRegistration;

public abstract class Component <TContent extends ContentComponent>
        implements EventReceiver, RenderUpdatable, Inspectable
{
    private Component next;
    private final ComponentObject componentObject;
    private final TContent contentComponent;

    public Component(ComponentObject componentObject, TContent contentComponent)
    {
        this.componentObject = componentObject;
        this.contentComponent = contentComponent;
    }

    public Component getNext()
    {
        return next;
    }

    public void setNext(Component next)
    {
        this.next = next;
    }

    public Map getMap()
    {
        return getComponentObject().getMap();
    }

    public <T extends Map> T getMap(Class<T> tClass)
    {
        return getComponentObject().getMap(tClass);
    }

    public void update(float dt)
    {
    }

    public void render(Batch batch, RenderContext context)
    {
    }

    public void init()
    {
    }

    public void release() {}

    public ComponentObject getComponentObject()
    {
        return componentObject;
    }

    public TContent getContentComponent()
    {
        return contentComponent;
    }

    @Override
    public int getZIndex()
    {
        return 0;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ContentComponent> getComponentClass()
    {
        return (Class<? extends ContentComponent>)((Object)this).getClass();
    }

    @Override
    public void inspect(PropertiesRegistration registration)
    {
        //
    }

    @Override
    public int getLayer()
    {
        return 0;
    }

    public void updated(ActiveData owner)
    {
        BrainOut.EventMgr.sendDelayedEvent(ComponentUpdatedEvent.obtain(this, owner));
    }

    public void updated(ActiveData owner, ComponentUpdatedEvent.Predicate predicate)
    {
        if (owner == null)
            return;

        BrainOut.EventMgr.sendDelayedEvent(ComponentUpdatedEvent.obtain(this, owner, predicate));
    }
}
