package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.InstrumentParticleComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentParticleComponent")
public class InstrumentParticleComponent extends ContentComponent
{

    @Override
    public InstrumentParticleComponentData getComponent(ComponentObject componentObject)
    {
        return new InstrumentParticleComponentData((InstrumentData) componentObject, this);
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
