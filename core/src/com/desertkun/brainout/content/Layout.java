package com.desertkun.brainout.content;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Layout")
public class Layout extends OwnableContent
{
    private String key;
    private int order;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        key = jsonData.getString("key", "");
        order = jsonData.getInt("order", 0);
    }

    public String getKey()
    {
        return key;
    }

    public int getOrder()
    {
        return order;
    }
}
