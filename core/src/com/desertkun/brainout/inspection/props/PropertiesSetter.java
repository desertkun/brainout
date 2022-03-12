package com.desertkun.brainout.inspection.props;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.common.editor.EditorProperty;
import com.desertkun.brainout.inspection.*;
import com.desertkun.brainout.utils.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertiesSetter extends PropertiesRegistration
{
    private final Array<EditorProperty> properties;

    public PropertiesSetter(Array<EditorProperty> properties)
    {
        this.properties = properties;
    }

    private EditorProperty getProperty(String name)
    {
        for (EditorProperty property: properties)
        {
            if (property.name.equals(name))
            {
                return property;
            }
        }

        return null;
    }

    @Override
    protected void doInspect(Inspectable inspectable)
    {
        // inspect fields

        final Field[] fields = inspectable.getClass().getFields();

        for (final Field field : fields)
        {
            final InspectableProperty property = field.getAnnotation(InspectableProperty.class);

            if (property != null)
            {
                EditorProperty prop = getProperty(property.name());

                if (prop != null)
                {
                    Object v = InstectableValue.setValue(prop.value, field.getType(), prop.data);

                    try
                    {
                        field.set(inspectable, v);
                    }
                    catch (IllegalAccessException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        // inspect setters + getters

        final Method[] methods = inspectable.getClass().getMethods();

        ObjectMap<String, Pair<Method, InspectableGetter>> getters = new ObjectMap<String, Pair<Method, InspectableGetter>>();
        ObjectMap<String, Pair<Method, InspectableSetter>> setters = new ObjectMap<String, Pair<Method, InspectableSetter>>();

        for (final Method method: methods)
        {
            try
            {
                final InspectableGetter getter = InstectableValue.getInheritedAnnotation(InspectableGetter.class, method);

                if (getter != null)
                {
                    getters.put(getter.name(), new Pair<Method, InspectableGetter>(method, getter));
                }

                final InspectableSetter setter = InstectableValue.getInheritedAnnotation(InspectableSetter.class, method);

                if (setter != null)
                {
                    setters.put(setter.name(), new Pair<Method, InspectableSetter>(method, setter));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        for (ObjectMap.Entry<String, Pair<Method, InspectableGetter>> entry: getters)
        {
            String name = entry.key;

            final Pair<Method, InspectableGetter> getterPair = entry.value;
            final Pair<Method, InspectableSetter> setterPair = setters.get(name);

            if (getterPair != null && setterPair != null)
            {
                EditorProperty prop = getProperty(name);

                if (prop != null)
                {
                    Method method = setterPair.first;

                    Object v = InstectableValue.setValue(prop.value, getterPair.first.getReturnType(), prop.data);

                    try
                    {
                        method.invoke(inspectable, new Object[]{v});
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
