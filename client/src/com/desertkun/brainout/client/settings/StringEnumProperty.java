package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.utils.ObjectMap;

public class StringEnumProperty extends StringProperty implements Property.SelectProperty
{
    private ObjectMap<String, String> options;

    public StringEnumProperty(String name, String localization, String def)
    {
        super(name, localization, def);

        options = new ObjectMap<>();
    }

    public StringEnumProperty(String name, String localization, String def, Properties properties)
    {
        this(name, localization, def);

        properties.addProperty(this);
    }

    public void addOption(String option, String localization)
    {
        options.put(option, localization);
    }

    public ObjectMap<String, String> getOptions()
    {
        return options;
    }

    @Override
    public void getOptions(ObjectMap<String, String> values)
    {
        values.putAll(options);
    }

    @Override
    public String getSelectValue()
    {
        return getValue();
    }

    @Override
    public boolean selectValue(String value)
    {
        setValue(value);
        return update();
    }

    @Override
    public boolean update()
    {
        return false;
    }

    public void clear()
    {
        options.clear();
    }
}
