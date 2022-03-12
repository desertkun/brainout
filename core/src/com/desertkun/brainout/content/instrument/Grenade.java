package com.desertkun.brainout.content.instrument;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.instrument.GrenadeData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.instrument.Grenade")
public class Grenade extends ThrowableInstrument
{
    private float delay;

    @Override
    public GrenadeData getData(String dimension)
    {
        return new GrenadeData(this, dimension);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        delay = jsonData.getFloat("delay", 5);
    }

    public float getDelay()
    {
        return delay;
    }
}
