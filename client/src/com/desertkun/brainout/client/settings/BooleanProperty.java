package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class BooleanProperty extends Property<Boolean> implements Property.CheckboxProperty
{
    public BooleanProperty(String name, String localization, boolean def)
    {
        super(name, localization);

        setValue(def);
    }

    public BooleanProperty(String name, String localization, boolean def, Properties properties)
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
        setValue(jsonData.getBoolean(getName(), getValue()));
    }

    @Override
    public boolean isChecked()
    {
        return getValue();
    }

    @Override
    public boolean check(boolean value)
    {
        return setValue(value);
    }
}
