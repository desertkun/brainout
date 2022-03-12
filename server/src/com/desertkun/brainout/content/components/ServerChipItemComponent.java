package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.components.ServerChipItemComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerChipItemComponent")
public class ServerChipItemComponent extends ServerItemComponent
{
    private boolean atSpawnPoint;

    @Override
    public ServerChipItemComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerChipItemComponentData((ItemData)componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.atSpawnPoint = jsonData.getBoolean("atSpawnPoint", false);
    }

    public boolean isAtSpawnPoint()
    {
        return atSpawnPoint;
    }
}
