package com.desertkun.brainout.inspection.props;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.common.editor.EditorProperty;
import com.desertkun.brainout.inspection.*;
import com.desertkun.brainout.utils.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PropertiesGetter extends PropertiesRegistration
{
    private Array<EditorProperty> properties;

    public PropertiesGetter()
    {
        properties = new Array<>();
    }

    public Array<EditorProperty> getProperties()
    {
        return properties;
    }

    private EditorProperty newProperty(String name)
    {
        for (EditorProperty property : properties)
        {
            if (property.name.equals(name))
            {
                return null;
            }
        }

        EditorProperty property = new EditorProperty();
        property.name = name;
        properties.add(property);
        return property;
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
                EditorProperty prop = newProperty(property.name());

                if (prop != null)
                {
                    try
                    {
                        prop.data = InstectableValue.getValue(field.get(inspectable), property.value());
                        prop.kind = property.kind();
                        prop.value = property.value();

                        if (property.className().isEmpty())
                        {
                            prop.clazz = BrainOut.R.getClassName(field.getType());
                        }
                        else
                        {
                            prop.clazz = property.className();
                        }
                    } catch (IllegalAccessException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }

        // inspect setters + getters

        final Method[] methods = inspectable.getClass().getMethods();

        ObjectMap<String, Pair<Method, InspectableGetter>> getters = new ObjectMap<>();
        ObjectMap<String, Pair<Method, InspectableSetter>> setters = new ObjectMap<>();

        for (final Method method: methods)
        {
            try
            {
                final InspectableGetter getter = InstectableValue.getInheritedAnnotation(InspectableGetter.class, method);

                if (getter != null)
                {
                    getters.put(getter.name(), new Pair<>(method, getter));
                }

                final InspectableSetter setter = InstectableValue.getInheritedAnnotation(InspectableSetter.class, method);

                if (setter != null)
                {
                    setters.put(setter.name(), new Pair<>(method, setter));
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
                EditorProperty prop = newProperty(name);

                if (prop != null)
                {
                    try
                    {
                        Method method = getterPair.first;
                        Object object = method.invoke(inspectable);

                        prop.data = InstectableValue.getValue(object, getterPair.second.value());
                        prop.kind = getterPair.second.kind();
                        prop.value = getterPair.second.value();
                        prop.clazz = BrainOut.R.getClassName(getterPair.first.getReturnType());
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
