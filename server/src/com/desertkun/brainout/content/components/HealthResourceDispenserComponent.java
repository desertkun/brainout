package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.components.HealthResourceDispenserComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.HealthResourceDispenserComponent")
public class HealthResourceDispenserComponent extends ResourceDispenserComponent
{
    private float amount;

    public HealthResourceDispenserComponent()
    {
        this.amount = 0;
    }

    @Override
    public HealthResourceDispenserComponentData getComponent(ComponentObject componentObject)
    {
        return new HealthResourceDispenserComponentData(componentObject, this);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        amount = jsonData.getFloat("amount", 0);
    }

    public float getAmount()
    {
        return amount;
    }
}
