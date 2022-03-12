package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveAddStatOnDestroyComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ActiveAddStatOnDestroyComponent")
public class ActiveAddStatOnDestroyComponent extends ContentComponent
{
    private String stat;

    @Override
    public ActiveAddStatOnDestroyComponentData getComponent(ComponentObject componentObject)
    {
        return new ActiveAddStatOnDestroyComponentData((ActiveData) componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        stat = jsonData.getString("stat");
    }

    public String getStat()
    {
        return stat;
    }
}
