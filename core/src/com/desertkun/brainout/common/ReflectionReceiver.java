package com.desertkun.brainout.common;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import static com.esotericsoftware.minlog.Log.*;

public class ReflectionReceiver
{
    private final HashMap<Class, Method> classToMethod;

    public ReflectionReceiver()
    {
        classToMethod = new HashMap<Class, Method>();
    }

    public boolean received(Object object)
    {
        return received(object, this);
    }

    public boolean received(Object object, Object to)
    {
        //if (Log.DEBUG) Log.debug("Got object from client " + object.toString());

        Class type = object.getClass();
        Method method = classToMethod.get(type);
        if (method == null)
        {
            if (classToMethod.containsKey(type)) return false; // Only fail on the first attempt to find the method.

            try
            {
                method = to.getClass().getMethod("received", new Class[] {type});
            }
            catch (SecurityException ex)
            {
                if (ERROR) error("kryonet", "Unable to access method: received(Connection, " + type.getName() + ")", ex);
                return false;
            }
            catch (NoSuchMethodException ex)
            {
                if (DEBUG)
                    debug("kryonet",
                            "Unable to find listener method: " + getClass().getName() + "#received(Connection, " + type.getName() + ")");
                return false;
            }
            finally
            {
                classToMethod.put(type, method);
            }
        }
        try
        {
            long time = System.nanoTime() / 1000;
            boolean result = (Boolean)method.invoke(to, object);
            long passed = System.nanoTime() / 1000 - time;
            if (passed > 1000)
            {
                if (INFO) info("app", "Method received " + type.getName() + " took " + passed + " us");
            }

            return result;
        }
        catch (Throwable ex)
        {
            if (ex instanceof InvocationTargetException && ex.getCause() != null) ex = ex.getCause();
            if (ex instanceof RuntimeException) throw (RuntimeException)ex;
            throw new RuntimeException("Error invoking method: " + getClass().getName() + "#received("
                    + type.getName() + ")", ex);
        }
    }
}
