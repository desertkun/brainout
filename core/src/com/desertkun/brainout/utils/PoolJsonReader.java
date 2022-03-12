package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.*;
import com.esotericsoftware.minlog.Log;

public class PoolJsonReader extends JsonReader
{
    private Array<JsonValue> free;
    private Array<JsonValue> used;

    public PoolJsonReader()
    {
        free = new Array<>();
        used = new Array<>();
    }

    @Override
    protected JsonValue newJsonValue(JsonValue.ValueType valueType)
    {
        if (free.size > 0)
        {
            JsonValue n = free.pop();
            used.add(n);
            n.setType(valueType);
            return n;
        }

        JsonValue n = new JsonValue(valueType);
        used.add(n);
        return n;
    }

    @Override
    protected JsonValue newJsonValue(double value, String stringValue)
    {
        if (free.size > 0)
        {
            JsonValue n = free.pop();
            used.add(n);
            n.set(value, stringValue);
            return n;
        }

        JsonValue n = new JsonValue(value, stringValue);
        used.add(n);
        return n;
    }

    @Override
    protected JsonValue newJsonValue(long value, String stringValue)
    {
        if (free.size > 0)
        {
            JsonValue n = free.pop();
            used.add(n);
            n.set(value, stringValue);
            return n;
        }

        JsonValue n = new JsonValue(value, stringValue);
        used.add(n);
        return n;
    }

    @Override
    protected JsonValue newJsonValue(boolean value)
    {
        if (free.size > 0)
        {
            JsonValue n = free.pop();
            used.add(n);
            n.set(value);
            return n;
        }

        JsonValue n = new JsonValue(value);
        used.add(n);
        return n;
    }

    @Override
    protected JsonValue newJsonValue(String value)
    {
        if (free.size > 0)
        {
            JsonValue n = free.pop();
            used.add(n);
            n.set(value);
            return n;
        }

        JsonValue n = new JsonValue(value);
        used.add(n);
        return n;
    }

    public void free()
    {
        if (Log.INFO) Log.info("Objects used: " + used.size);
        for (JsonValue value : used)
        {
            value.set(null);
            value.name = null;
            value.next = null;
            value.prev = null;
            value.child = null;
            value.parent = null;
            value.size = 0;
        }
        free.addAll(used);
        used.clear();
    }
}
