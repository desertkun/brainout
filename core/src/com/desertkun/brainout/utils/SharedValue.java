package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.ObjectMap;

public class SharedValue<T>
{
    private String key;
    private Container<T> container;

    public interface Container<T>
    {
        T get(String key, T def);
        void set(String key, T value);
        void clear();
    }

    public static class MapContainer<T> implements Container<T>
    {
        private ObjectMap<String, T> values = new ObjectMap<>();

        @Override
        public T get(String key, T def)
        {
            return values.get(key, def);
        }

        @Override
        public void set(String key, T value)
        {
            values.put(key, value);
        }

        public void clear()
        {
            values.clear();
        }
    }

    public static class SimpleContainer<T> implements Container<T>
    {
        private T value;

        @Override
        public T get(String key, T def)
        {
            return value == null ? def : value;
        }

        @Override
        public void set(String key, T value)
        {
            this.value = value;
        }

        @Override
        public void clear()
        {
            this.value = null;
        }
    }

    public SharedValue()
    {
    }

    public SharedValue(String key, Container<T> container)
    {
        init(key, container);
    }

    public void init(String key, Container<T> container)
    {
        this.key = key;
        this.container = container;
    }

    public void setContainer(Container<T> container)
    {
        this.container = container;
    }

    public T get(T def)
    {
        if (key == null) return def;
        if (container == null) return def;
        return container.get(key, def);
    }

    public void set(T value)
    {
        if (container == null) return;
        container.set(key, value);
    }

    public String getKey()
    {
        return key;
    }
}
