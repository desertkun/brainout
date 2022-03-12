package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ClientDeckOfCardsComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientDeckOfCardsComponent")
public class ClientDeckOfCardsComponent extends ClientActiveActivatorComponent
{
    private String back;

    @Override
    public ClientDeckOfCardsComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientDeckOfCardsComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        back = jsonData.getString("back");
    }

    public String getBack()
    {
        return back;
    }
}
