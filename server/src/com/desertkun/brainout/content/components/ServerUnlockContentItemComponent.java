package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.ServerChipItemComponentData;
import com.desertkun.brainout.data.components.ServerUnlockContentItemComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;

@Reflect("content.components.ServerUnlockContentItemComponent")
public class ServerUnlockContentItemComponent extends ServerItemComponent
{
    @Override
    public ServerUnlockContentItemComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerUnlockContentItemComponentData((ItemData)componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
    }
}
