package com.desertkun.brainout.inspection;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.utils.ObjectMapSerializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

public class InstectableValue
{
    public static String getValue(Object object, PropertyValue value)
    {
        switch (value)
        {
            case vString:
            {
                return object != null ? object.toString() : "";
            }

            case vBoolean:
            {
                return ((Boolean)object) ? "true" : "false";
            }

            case vStringMap:
            {
                ObjectMap<String, String> asObjectMap = ((ObjectMap) object);

                return BrainOut.R.JSON.toJson(new ObjectMapSerializer(asObjectMap));
            }

            case vContent:
            {
                if (object instanceof Content)
                {
                    Content asContent = ((Content) object);
                    return asContent.getID();
                }
                else
                {
                    return "";
                }
            }

            case vFloat:
            {
                return String.valueOf(((Float) object).floatValue());
            }

            case vInt:
            {
                return String.valueOf(((Integer) object).intValue());
            }

            case vEnum:
            {
                Enum en = ((Enum) object);

                return en.toString();
            }
        }

        return "";
    }

    public static Object setValue(PropertyValue value, Class clazz, String asString)
    {
        switch (value)
        {
            case vString:
            {
                return asString;
            }

            case vBoolean:
            {
                return (Boolean)asString.equals("true");
            }

            case vStringMap:
            {
                ObjectMapSerializer s = BrainOut.R.JSON.fromJson(ObjectMapSerializer.class, asString);

                return s.getObjectMap();
            }
            case vContent:
            {
                return BrainOut.ContentMgr.get(asString);
            }
            case vFloat:
            {
                try
                {
                    return Float.valueOf(asString);
                }
                catch (NumberFormatException e)
                {
                    return 0;
                }
            }
            case vInt:
            {
                try
                {
                    return Integer.valueOf(asString);
                }
                catch (NumberFormatException e)
                {
                    return 0;
                }
            }
            case vEnum:
            {
                Class<? extends Enum> enumClass = ((Class<? extends Enum>) clazz);

                return Enum.valueOf(enumClass, asString);
            }
        }

        return null;
    }

    public static <A extends Annotation> A getInheritedAnnotation(
            Class<A> annotationClass, AnnotatedElement element)
    {
        A annotation = element.getAnnotation(annotationClass);
        if (annotation == null && element instanceof Method)
            annotation = getOverriddenAnnotation(annotationClass, (Method) element);
        return annotation;
    }

    private static <A extends Annotation> A getOverriddenAnnotation(
            Class<A> annotationClass, Method method)
    {
        final Class<?> methodClass = method.getDeclaringClass();
        final String name = method.getName();
        final Class<?>[] params = method.getParameterTypes();

        // prioritize all superclasses over all interfaces
        final Class<?> superclass = methodClass.getSuperclass();
        if (superclass != null)
        {
            final A annotation =
                    getOverriddenAnnotationFrom(annotationClass, superclass, name, params);
            if (annotation != null)
                return annotation;
        }

        // depth-first search over interface hierarchy
        for (final Class<?> intf : methodClass.getInterfaces())
        {
            final A annotation =
                    getOverriddenAnnotationFrom(annotationClass, intf, name, params);
            if (annotation != null)
                return annotation;
        }

        return null;
    }

    private static <A extends Annotation> A getOverriddenAnnotationFrom(
            Class<A> annotationClass, Class<?> searchClass, String name, Class<?>[] params)
    {
        try
        {
            final Method method = searchClass.getMethod(name, params);
            final A annotation = method.getAnnotation(annotationClass);
            if (annotation != null)
                return annotation;
            return getOverriddenAnnotation(annotationClass, method);
        }
        catch (final NoSuchMethodException e)
        {
            return null;
        }
    }
}
