package com.desertkun.brainout.client.settings;


import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.L;

public class IntegerEnumProperty extends IntegerProperty implements Property.SelectProperty
{
    private ObjectMap<Integer, String> options;

    public IntegerEnumProperty(String name, String localization, Integer def)
    {
        super(name, localization, def);

        options = new ObjectMap<Integer, String>();
    }

    public IntegerEnumProperty(String name, String localization, Integer def, Properties properties)
    {
        this(name, localization, def);

        properties.addProperty(this);
    }

    public void addOption(Integer option, String localization)
    {
        options.put(option, localization);
    }

    public ObjectMap<Integer, String> getOptions()
    {
        return options;
    }

    @Override
    public void getOptions(ObjectMap<String, String> values)
    {
        for (ObjectMap.Entry<Integer, String> option : options)
        {
            values.put(String.valueOf(option.key), L.get(option.value));
        }
    }

    @Override
    public String getSelectValue()
    {
        return String.valueOf(getValue());
    }

    @Override
    public boolean selectValue(String value)
    {
        setValue(Integer.valueOf(value));
        return update();
    }

    @Override
    public boolean update()
    {
        return false;
    }
}
