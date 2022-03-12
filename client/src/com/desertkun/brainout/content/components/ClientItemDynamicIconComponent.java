package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.ClientItemComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientItemDynamicIconComponent")
public class ClientItemDynamicIconComponent extends ContentComponent
{
    private Array<Integer> ranges;
    private String prefix;
    private String fallback;

    public ClientItemDynamicIconComponent()
    {
        ranges = new Array<>();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        prefix = jsonData.getString("prefix");
        fallback = jsonData.getString("fallback");

        JsonValue ranges = jsonData.get("ranges");

        for (JsonValue range : ranges)
        {
            this.ranges.add(range.asInt());
        }
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getFallback()
    {
        return fallback;
    }

    public Array<Integer> getRanges()
    {
        return ranges;
    }
}
