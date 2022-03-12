package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;

public abstract class Property<T> implements Json.Serializable
{
    private final String localization;
    private String name;
    private T value;

    public interface CheckboxProperty
    {
        boolean isChecked();
        boolean check(boolean value);
    }

    public interface SelectProperty
    {
        void getOptions(ObjectMap<String, String> values);
        String getSelectValue();
        boolean selectValue(String value);

        boolean update();
    }

    public interface TrackbarProperty
    {
        Integer getValue();
        boolean setValue(Integer value);
        int getMin();
        int getMax();

        void update();
    }

    public interface KeyProperty
    {
        Integer getValue();
        void setKeyValue(int keycode);
    }

    public Property(String name, String localization)
    {
        this.name = name;
        this.localization = localization;
    }

    public boolean setValue(T value)
    {
        this.value = value;

        return false;
    }

    public T getValue()
    {
        return value;
    }

    public String getName()
    {
        return name;
    }

    public String getLocalization()
    {
        return localization;
    }
}