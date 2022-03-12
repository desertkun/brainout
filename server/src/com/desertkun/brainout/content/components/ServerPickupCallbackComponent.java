package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.ServerPickupCallbackComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerPickupCallbackComponent")
public class ServerPickupCallbackComponent extends ContentComponent
{
    private boolean block;

    @Override
    public ServerPickupCallbackComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerPickupCallbackComponentData((ItemData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        block = jsonData.getBoolean("block", false);
    }

    public boolean isBlock()
    {
        return block;
    }
}
