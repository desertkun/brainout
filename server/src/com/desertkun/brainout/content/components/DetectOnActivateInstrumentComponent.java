package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.DetectOnActivateInstrumentComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.DetectOnActivateInstrumentComponent")
public class DetectOnActivateInstrumentComponent extends ContentComponent
{
    private String detectClass;

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new DetectOnActivateInstrumentComponentData((InstrumentData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.detectClass = jsonData.getString("detectClass");
    }

    public String getDetectClass()
    {
        return detectClass;
    }
}
