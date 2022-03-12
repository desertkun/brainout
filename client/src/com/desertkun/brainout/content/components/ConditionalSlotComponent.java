package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.ClientWeaponComponentData;
import com.desertkun.brainout.data.components.ConditionalSlotComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.WeaponData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ConditionalSlotComponent")
public class ConditionalSlotComponent extends ContentComponent
{
    private ObjectMap<String, String> conditions;

    public ConditionalSlotComponent()
    {
        conditions = new ObjectMap<>();
    }

    @Override
    public ConditionalSlotComponentData getComponent(ComponentObject componentObject)
    {
        return new ConditionalSlotComponentData((InstrumentData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {
        //
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        if (jsonData.has("conditions"))
        {
            for (JsonValue value : jsonData.get("conditions"))
            {
                this.conditions.put(value.name(), value.asString());
            }
        }
    }

    public ObjectMap<String, String> getConditions()
    {
        return conditions;
    }
}
