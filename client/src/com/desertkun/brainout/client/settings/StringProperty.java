package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.L;

public class StringProperty extends Property<String>
{
    public StringProperty(String name, String localization, String def)
    {
        super(name, localization);

        setValue(def);
    }

    public StringProperty(String name, String localization, String def, Properties properties)
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
        setValue(jsonData.getString(getName(), getValue()));
    }
}
