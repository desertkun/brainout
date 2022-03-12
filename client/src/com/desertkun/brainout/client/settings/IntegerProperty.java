package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.L;

public class IntegerProperty extends Property<Integer>
{
    public IntegerProperty(String name, String localization, Integer def)
    {
        super(name, localization);

        setValue(def);
    }

    public IntegerProperty(String name, String localization, Integer def, Properties properties)
    {
        this(name, localization, def);

        properties.addProperty(this);
    }

    @Override
    public void write(Json json)
    {
        json.writeValue(getName(), getValue());
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        setValue(jsonData.getInt(getName(), getValue()));
    }
}
