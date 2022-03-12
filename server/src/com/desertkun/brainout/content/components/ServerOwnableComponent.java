package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.content.OwnableContent;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

public abstract class ServerOwnableComponent extends ContentComponent
{
    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    public abstract void owned(PlayerClient client, OwnableContent content);

    @Override
    public void read(Json json, JsonValue jsonData)
    {

    }
}
