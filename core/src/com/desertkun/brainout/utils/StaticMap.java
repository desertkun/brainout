package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.BrainOut;

import java.lang.reflect.Constructor;

public class StaticMap
{
    private static final int POOL = 128;

    private static class Pool
    {
        public Object[] objects;
        public int index;

        public Pool()
        {
            this.objects = new Object[POOL];
            this.index = 0;
        }

        public <T> T next(Class<T> classOf)
        {
            if (index >= POOL)
            {
                index = 0;
            }

            Object obj = this.objects[index];

            if (obj != null)
            {
                index++;
                return ((T) obj);
            }

            try
            {
                Constructor<?> constructor = classOf.getConstructor(EmptyClassArray);
                obj = constructor.newInstance(EmptyObjectArray);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }

            this.objects[index] = obj;

            index++;


            return ((T) obj);
        }
    }

    private static ObjectMap<Class, Pool> Objects = new ObjectMap<>();
    private static Object[] EmptyObjectArray = new Object[]{};
    private static Class[] EmptyClassArray = new Class[]{};

    public static <T> T get(Class<T> classOf)
    {
        Pool obj = Objects.get(classOf);

        if (obj != null)
        {
            return obj.next(classOf);
        }

        Pool newPool = new Pool();
        Objects.put(classOf, newPool);

        return newPool.next(classOf);
    }
}
