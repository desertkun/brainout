package com.desertkun.brainout.client.settings;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.L;

public class Properties extends Property<Properties> implements Json.Serializable
{
    private Array<Property> properties;

    public Properties(String name, String localization)
    {
        super(name, localization);

        this.properties = new Array<Property>();
    }

    public Properties(String name, String localization, Properties parent)
    {
        this(name, localization);

        if (parent != null)
        {
            parent.addProperty(this);
        }
    }

    public Array<Property> getProperties()
    {
        return properties;
    }

    public void addProperty(Property property)
    {
        properties.add(property);
    }

    @Override
    public void write(Json json)
    {
        json.writeObjectStart(getName());
        for (Property property : properties)
        {
            property.write(json);
        }
        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        JsonValue props = jsonData.get(getName());

        if (props != null)
        {
            for (Property property : properties)
            {
                property.read(json, props);
            }
        }
    }
}
