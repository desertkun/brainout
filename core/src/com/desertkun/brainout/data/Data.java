package com.desertkun.brainout.data;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.data.interfaces.ComponentWritable;
import com.desertkun.brainout.data.interfaces.DoNotSyncToClients;
import com.esotericsoftware.minlog.Log;

import java.io.StringWriter;
import java.lang.reflect.Constructor;

public abstract class Data extends ComponentObject implements ComponentWritable, Json.Serializable
{
    public Data(Content content, String dimension)
    {
        super(content, dimension);
    }

    public interface ComponentWriter
    {
        ComponentWriter TRUE = (ownerId1, data, component) -> true;

        boolean canSend(int ownerId, Data data, Component component);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        setDimension(jsonData.getString("d", getDimension()));

        JsonValue components = jsonData.get("cmp");

        if (components == null)
            components = jsonData.get("components");

        if (components != null)
        {
            for (JsonValue component: components)
            {
                String classId = component.name();
                try
                {
                    Class componentClass;

                    try
                    {
                        componentClass = BrainOut.R.forName(classId);
                    }
                    catch (Exception e)
                    {
                        if (Log.ERROR) Log.error(e.getMessage());
                        continue;
                    }

                    Component cmp = getComponent(componentClass);
                    boolean add = false;

                    if (cmp == null)
                    {
                        try
                        {
                            Constructor componentConstructor = componentClass.getConstructor(ComponentObject.class);
                            Object obj = componentConstructor.newInstance(new Object[]{this});
                            cmp = (Component) obj;
                            add = true;
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();

                            if (Log.ERROR) Log.error("Component not found for data: " + getClass().getCanonicalName());
                            if (Log.ERROR) Log.error("Component: " + e.getMessage());

                            continue;
                        }
                    }

                    if (Component.class.isAssignableFrom(componentClass) &&
                            Json.Serializable.class.isAssignableFrom(componentClass))
                    {
                        Json.Serializable asSerializable = ((Json.Serializable) cmp);

                        asSerializable.read(json, component);
                    }

                    if (add)
                    {
                        addComponent(cmp);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void write(Json json)
    {
        String d = getDimension();

        if (d != null)
            json.writeValue("d", d);
    }

    public static class ComponentSerializer
    {
        public static String toJson(ComponentWritable data, ComponentWriter componentWriter, int owner)
        {
            StringWriter stringWriter = new StringWriter();
            Json json = new Json();

            BrainOut.R.tag(json);

            json.setWriter(stringWriter);

            json.writeObjectStart();
            data.write(json, componentWriter, owner);
            json.writeObjectEnd();

            return stringWriter.toString();
        }
    }

    @Override
    public void write(Json json, ActiveData.ComponentWriter componentWriter, int owner)
    {
        write(json);

        Component it = getFistComponent();

        if (it != null && componentWriter != null)
        {
            boolean hasAnyComponent = false;

            while (it != null)
            {
                if (componentWriter.canSend(owner, this, it))
                {
                    //if (Log.DEBUG) Log.debug("Writing component " + component.getClass().getCanonicalName());

                    if (it instanceof Json.Serializable && !(it instanceof DoNotSyncToClients))
                    {
                        if (!hasAnyComponent)
                        {
                            json.writeObjectStart("cmp");
                            hasAnyComponent = true;
                        }

                        json.writeValue(BrainOut.R.getClassName(it.getClass()), it);
                    }
                    /*
                    else
                    {
                        throw new RuntimeException("Class " + component.getClass().getCanonicalName() + " is not serializable");
                    }
                    */
                }

                it = it.getNext();
            }

            if (hasAnyComponent)
            {
                json.writeObjectEnd();
            }
        }
    }
}
