package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.esotericsoftware.minlog.Log;

@Reflect("content.components.ServerWalkietalkieActivatorComponent")
public class ServerWalkietalkieActivatorComponent extends ServerItemActivatorComponent
{
    public ServerWalkietalkieActivatorComponent()
    {
    }

    @Override
    public boolean activate(PlayerClient playerClient, PlayerData playerData, int quality)
    {
        return false;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);
    }
}
