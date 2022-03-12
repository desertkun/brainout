package com.desertkun.brainout.reflection;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import eu.infomas.annotation.AnnotationDetector;
import eu.infomas.annotation.Builder;
import eu.infomas.annotation.Cursor;
import eu.infomas.annotation.ReporterFunction;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
public class Reflection
{
    private ObjectMap<String, Class> classesList;
    private ObjectMap<Class, String> classNames;
    public Json JSON;

    public Reflection()
    {
        this.classesList = new ObjectMap<>();
        this.classNames = new ObjectMap<>();
    }

    @SuppressWarnings("unchecked")
    public boolean init()
    {
        try
        {
            AnnotationDetector.
                scanClassPath().
                filter(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String name)
                    {
                        return name.startsWith("com/desertkun") || name.startsWith("com\\desertkun");
                    }
                }).
                forAnnotations(Reflect.class).
                on(ElementType.TYPE).
                collect(cursor ->
                {
                    Class clazz = cursor.getType();
                    Reflect reflect = cursor.getAnnotation(Reflect.class);
                    addClass(reflect.value(), clazz, true);
                    return reflect;
                });
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        try
        {
            AnnotationDetector.
                scanClassPath().
                filter(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String name)
                    {
                        return name.startsWith("com/desertkun") || name.startsWith("com\\desertkun");
                    }
                }).
                forAnnotations(ReflectAlias.class).
                on(ElementType.TYPE).
                collect(cursor ->
                {
                    Class clazz = cursor.getType();
                    ReflectAlias reflectAlias = cursor.getAnnotation(ReflectAlias.class);
                    addClass(reflectAlias.value(), clazz, false);
                    return reflectAlias;
                });
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        addClass("com.badlogic.gdx.utils.ObjectMap", ObjectMap.class, true);
        addClass("com.badlogic.gdx.utils.Array", Array.class, true);

        addClass("java.lang.String", String.class, true);
        addClass("java.lang.Boolean", Boolean.class, true);
        addClass("java.lang.Integer", Integer.class, true);
        addClass("java.lang.Float", Float.class, true);
        addClass("float", float.class, true);
        addClass("int", int.class, true);
        addClass("boolean", boolean.class, true);
        addClass("java.lang.Enum", Enum.class, true);
        addClass("java.lang.Object", Object.class, true);

        JSON = new Json();
        tag(JSON);

        return true;
    }

    @SuppressWarnings("unchecked")
    protected Object instantiate(Class clazz)
    {
        try
        {
            Constructor<?> constructor = clazz.getConstructor(new Class[]{});
            return constructor.newInstance(new Object[]{});
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected Object instantiate(Class clazz, Class[] argClasses, Object[] args)
    {
        try
        {
            Constructor<?> constructor = clazz.getConstructor(argClasses);
            return constructor.newInstance(args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean instanceOf(Class classOff, Object object)
    {
        return classOff.isInstance(object);
    }

    private void addClass(String className, Class clazz, boolean bothWays)
    {
        if (classesList.containsKey(className))
        {
            throw new RuntimeException("Double class registration: " + className);
        }
        classesList.put(className, clazz);

        if (bothWays)
        {
            classNames.put(clazz, className);
        }
    }

    public String getClassName(Class clazz)
    {
        String className = classNames.get(clazz);

        if (className == null)
        {
            throw new RuntimeException("Class " + clazz.toString() + " is not registered in the reflection.");
        }

        return className;
    }

    public Object newInstance(String className)
    {
        Class clazz = forName(className);

        if (clazz != null)
        {
            return instantiate(clazz);
        }

        return null;
    }

    public Object newInstance(String className, Class[] argClasses, Object[] args)
    {
        Class clazz = forName(className);

        if (clazz != null)
        {
            return instantiate(clazz, argClasses, args);
        }

        return null;
    }

    public Class forName(String className)
    {
        Class clazz = classesList.get(className);
        if (clazz == null)
        {
            throw new RuntimeException("Class " + className + " is not registered in the reflection.");
        }

        return clazz;
    }

    public void tag(Json json)
    {
        for (ObjectMap.Entry<String, Class> entry: classesList)
        {
            json.addClassTag(entry.key, entry.value);
        }
    }
}
