package com.desertkun.brainout.content.instrument;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.active.ThrowableActive;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.instrument.ThrowableInstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.instrument.ThrowableInstrument")
public class ThrowableInstrument extends Instrument
{
    private float throwPower;
    private ThrowableActive throwActive;

    @Override
    public InstrumentData getData(String dimension)
    {
        return new ThrowableInstrumentData(this, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        this.throwPower = jsonData.getFloat("throwPower");
        this.throwActive = ((ThrowableActive) BrainOut.ContentMgr.get(jsonData.getString("throwActive")));
    }

    public float getThrowPower()
    {
        return throwPower;
    }

    public ThrowableActive getThrowActive()
    {
        return throwActive;
    }
}
