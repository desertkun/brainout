package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.data.components.MedkitComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.MedkitComponent")
public class MedkitComponent extends ContentComponent
{
    private float health;
    private float period;
    private float amount;

    @Override
    public MedkitComponentData getComponent(ComponentObject componentObject)
    {
        return new MedkitComponentData((InstrumentData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {


    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        this.health = jsonData.getFloat("health");
        this.period = jsonData.getFloat("period");
        this.amount = jsonData.getFloat("amount");
    }

    public float getHealth()
    {
        return health;
    }

    public float getPeriod()
    {
        return period;
    }

    public float getAmount()
    {
        return amount;
    }
}
