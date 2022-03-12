package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.active.BotSpawnerData;
import com.desertkun.brainout.data.components.FreeplayBotSpawnerComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.FreeplayBotSpawnerComponent")
public class FreeplayBotSpawnerComponent extends ContentComponent
{
    private boolean enabled;

    @Override
    public FreeplayBotSpawnerComponentData getComponent(ComponentObject componentObject)
    {
        return new FreeplayBotSpawnerComponentData((BotSpawnerData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonValue)
    {
        enabled = jsonValue.getBoolean("enabled");
    }

    public boolean isEnabled()
    {
        return enabled;
    }
}
