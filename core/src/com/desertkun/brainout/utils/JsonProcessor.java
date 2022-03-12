package com.desertkun.brainout.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.*;
import com.desertkun.brainout.BrainOut;
import com.esotericsoftware.minlog.Log;

public class JsonProcessor<T extends Json.Serializable>
{
    public JsonProcessor()
    {
    }

    public void objectCreated(T obj, String name) {}
    public void objectLoaded(T obj) {}

    public boolean checkTag(JsonValue data)
    {
        if (data.has("tag"))
        {
            return BrainOut.getInstance().hasTag(data.getString("tag"));
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public final void load(Json json, JsonValue data)
    {
        if (data == null) return;

        ArrayMap<T, JsonValue> items = new ArrayMap<>();


        for (JsonValue child : data)
        {
            String id = child.name();

            JsonValue classValue = child.get("class");

            if (classValue == null)
            {
                throw new RuntimeException("Child " + id + " has no class field!");
            }

            String className = classValue.asString();

            try
            {
                if (checkTag(child))
                {
                    T item = (T)BrainOut.R.newInstance(className);

                    items.put(item, child);
                    objectCreated(item, id);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                if (Log.ERROR) Log.error("unknown content class: " + className);
            }
        }


        for (T item: items.keys())
        {
            if (item == null)
                continue;

            JsonValue oData = items.get(item);

            if (oData.has("extends"))
            {
                String extendsId = oData.getString("extends");
                item.read(json, data.get(extendsId));
            }

            try
            {
                item.read(json, oData);
            }
            catch (Exception e)
            {
                if (Log.ERROR) Log.error("Error while processing: " + item.toString());
                throw e;
            }
            objectLoaded(item);
        }

    }
}
