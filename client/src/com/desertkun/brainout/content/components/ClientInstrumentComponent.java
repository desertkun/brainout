package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ClientInstrumentComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientInstrumentComponent")
public class ClientInstrumentComponent extends ContentComponent
{
    @Override
    public ClientInstrumentComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientInstrumentComponentData((InstrumentData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {

    }
}
