package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

public class ObjectMapSerializer implements Json.Serializable
{
    private final ObjectMap<String, String> objectMap;

    public ObjectMapSerializer(ObjectMap<String, String> objectMap)
    {
        this.objectMap = objectMap;
    }

    public ObjectMap<String, String> getObjectMap()
    {
        return objectMap;
    }

    public ObjectMapSerializer()
    {
        this.objectMap = new ObjectMap<String, String>();
    }

    @Override
    public void write(Json json)
    {
        for (ObjectMap.Entry<String, String> entry: objectMap)
        {
            json.writeValue(entry.key, entry.value);
        }
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        objectMap.clear();

        if (jsonData.isObject())
        {
            for (JsonValue v: jsonData)
            {
                objectMap.put(v.name(), v.asString());
            }
        }
    }
}
