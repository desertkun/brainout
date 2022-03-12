package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientMenuActivatorComponentData;
import com.desertkun.brainout.data.components.ClientSafeActivatorComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientMenuActivatorComponent")
public class ClientMenuActivatorComponent extends ClientActiveActivatorComponent
{
    private String menu;

    public ClientMenuActivatorComponent()
    {
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ClientMenuActivatorComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        menu = jsonData.getString("menu");
    }

    public String getMenu()
    {
        return menu;
    }
}
