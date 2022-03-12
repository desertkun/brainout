package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ServerDeckOfCardsComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ServerDeckOfCardsComponent")
public class ServerDeckOfCardsComponent extends ContentComponent
{
    private Array<String> deck;

    public ServerDeckOfCardsComponent()
    {
        deck = new Array<>();
    }

    @Override
    public ServerDeckOfCardsComponentData getComponent(ComponentObject componentObject)
    {
        return new ServerDeckOfCardsComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonValue)
    {
        for (JsonValue value : jsonValue.get("deck"))
        {
            deck.add(value.asString());
        }
    }

    public Array<String> getDeck()
    {
        return deck;
    }
}
