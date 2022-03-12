package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.common.msg.client.ActivateItemMsg;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.consumable.ConsumableRecord;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientItemActivatorComponent")
public class ClientItemActivatorComponent extends ContentComponent
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

    public void activate(ConsumableRecord record)
    {
        BrainOutClient.ClientController.sendTCP(new ActivateItemMsg(record.getId()));
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {

    }
}
