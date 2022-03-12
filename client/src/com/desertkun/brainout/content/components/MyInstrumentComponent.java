package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.MyInstrumentComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

public abstract class MyInstrumentComponent extends ContentComponent
{
    @Override
    public abstract MyInstrumentComponentData getComponent(ComponentObject componentObject);

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {

    }
}
