package com.desertkun.brainout.data.components.base;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.data.interfaces.RenderUpdatable;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.EventReceiver;
import com.desertkun.brainout.inspection.Inspectable;
import com.desertkun.brainout.inspection.props.PropertiesRegistration;

import java.util.HashMap;

public class ComponentObject implements EventReceiver, RenderUpdatable, Inspectable
{
    private String dimension;
    private final Content content;
    private Component fistComponent, lastComponent;
    private short componentsCount;
    private HashMap<Class<? extends Component>, Component> componentsIndex;

    public Map getMap()
    {
        return Map.Get(this.dimension);
    }

    public <T extends Map> T getMap(Class<T> tClass)
    {
        return Map.Get(this.dimension, tClass);
    }

    public ComponentObject(Content content, String dimension)
    {
        this.dimension = dimension;
        this.fistComponent = null;
        this.lastComponent = null;
        this.componentsIndex = null;
        this.componentsCount = 0;

        this.content = content;

        if (content != null)
        {
            content.initComponentObject(this);
        }
    }

    public String getDimension()
    {
        return dimension;
    }

    public int getDimensionId()
    {
        return Map.GetDimensionId(dimension);
    }

    public void setDimension(String dimension)
    {
        this.dimension = dimension;
    }

    public Component getFistComponent()
    {
        return fistComponent;
    }

    public void addComponent(Component component)
    {
        if (lastComponent == null)
        {
            fistComponent = component;
        }
        else
        {
            lastComponent.setNext(component);
        }

        lastComponent = component;
        lastComponent.setNext(null);
        componentsCount++;
    }

    public void removeComponent(Component component, boolean release)
    {
        Component prev = fistComponent;

        if (prev == null)
            return;

        if (component == prev)
        {
            fistComponent = component.getNext();
            component.setNext(null);

            if (fistComponent == null)
                lastComponent = null;
        }
        else
        {
            while (true)
            {
                Component next = prev.getNext();

                if (next == null)
                {
                    return;
                }
                else if (next == component)
                {
                    break;
                }

                prev = next;
            }

            Component next = prev.getNext();

            if (next == lastComponent)
                lastComponent = prev;

            prev.setNext(next.getNext());
            next.setNext(null);
        }

        if (release)
        {
            component.release();
        }

        componentsCount--;

        if (componentsIndex != null)
            componentsIndex.remove(component.getClass());
    }

    public void removeComponent(Component component)
    {
        removeComponent(component, true);
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> classOf)
    {
        if (fistComponent == null)
            return null;

        if (componentsCount <= 4)
        {
            // if there less components than 4, don't even bother creaing map for it
            return findComponent(classOf);
        }

        if (componentsIndex == null)
        {
            T component = findComponent(classOf);

            if (component != null)
            {
                componentsIndex = new HashMap<>(2);
                componentsIndex.put(component.getClass(), component);
                return component;
            }

            return null;
        }
        else
        {
            T component = (T)componentsIndex.get(classOf);

            if (component != null)
            {
                return component;
            }

            component = findComponent(classOf);

            if (component != null)
            {
                componentsIndex.put(component.getClass(), component);
                return component;
            }

            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T findComponent(Class<T> classOf)
    {
        Component it = fistComponent;

        while (it != null)
        {
            if (it.getClass() == classOf)
                return (T)it;

            it = it.getNext();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T findComponentWithSubclass(Class<T> classOf)
    {
        Component it = fistComponent;

        while (it != null)
        {
            if (BrainOut.R.instanceOf(classOf, it))
                return (T)it;

            it = it.getNext();
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponentWithSubclass(Class<T> classOf)
    {
        if (fistComponent == null)
            return null;

        if (componentsCount <= 4)
        {
            // if there less components than 4, don't even bother creating map for it
            return findComponentWithSubclass(classOf);
        }

        if (componentsIndex == null)
        {
            T component = findComponentWithSubclass(classOf);

            if (component != null)
            {
                componentsIndex = new HashMap<>(2);
                componentsIndex.put(classOf, component);
                return component;
            }

            return null;
        }
        else
        {
            T component = (T)componentsIndex.get(classOf);

            if (component != null)
            {
                return component;
            }

            component = findComponentWithSubclass(classOf);

            if (component != null)
            {
                componentsIndex.put(classOf, component);
                return component;
            }

            return null;
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        Component it = fistComponent;

        while (it != null)
        {
            if (it.onEvent(event))
            {
                return true;
            }

            it = it.getNext();
        }

        return false;
    }

    @Override
    public void update(float dt)
    {
        Component it = fistComponent;

        while (it != null)
        {
            it.update(dt);
            it = it.getNext();
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        Component it = fistComponent;

        while (it != null)
        {
            it.render(batch, context);
            it = it.getNext();
        }
    }

    public void init()
    {
        initComponents();
    }

    public void release()
    {
        Component it = fistComponent;

        while (it != null)
        {
            it.release();
            it = it.getNext();
        }

        if (disposeOnRelease())
        {
            disposeComponents();
        }
    }

    public Content getContent()
    {
        return content;
    }

    public void initComponents()
    {
        Component it = fistComponent;

        while (it != null)
        {
            it.init();
            it = it.getNext();
        }
    }

    public boolean disposeOnRelease()
    {
        return true;
    }

    private void disposeComponents()
    {
        Component it = fistComponent;

        while (it != null)
        {
            Component next = it.getNext();
            it.setNext(null);
            it = next;
        }

        fistComponent = null;
        lastComponent = null;
        componentsCount = 0;

        if (componentsIndex != null)
        {
            componentsIndex.clear();
            componentsIndex = null;
        }
    }

    @Override
    public boolean hasRender()
    {
        Component it = fistComponent;

        while (it != null)
        {
            if (it.hasRender())
            {
                return true;
            }

            it = it.getNext();
        }

        return false;
    }

    @Override
    public int getZIndex()
    {
        return 0;
    }

    @Override
    public boolean hasUpdate()
    {
        Component it = fistComponent;

        while (it != null)
        {
            if (it.hasUpdate())
            {
                return true;
            }

            it = it.getNext();
        }

        return false;
    }

    @Override
    public void inspect(PropertiesRegistration registration)
    {
        Component it = fistComponent;

        while (it != null)
        {
            registration.inspectChild(it);

            it = it.getNext();
        }
    }

    @Override
    public int getLayer()
    {
        return 0;
    }
}
