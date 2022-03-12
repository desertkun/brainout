package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.LimitInstancesComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.LimitInstancesComponent")
public class LimitInstancesComponent extends ContentComponent
{
    private String itemsClass;
    private int instancesCount;

    @Override
    public LimitInstancesComponentData getComponent(ComponentObject componentObject)
    {
        return new LimitInstancesComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        itemsClass = jsonData.getString("itemsClass");
        instancesCount = jsonData.getInt("instancesCount", 1);
    }

    public String getItemsClass()
    {
        return itemsClass;
    }

    public int getInstancesCount()
    {
        return instancesCount;
    }
}
