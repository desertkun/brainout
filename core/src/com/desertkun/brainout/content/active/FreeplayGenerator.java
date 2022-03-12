package com.desertkun.brainout.content.active;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.instrument.Instrument;
import com.desertkun.brainout.data.active.FreeplayGeneratorData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.active.FreeplayGenerator")
public class FreeplayGenerator extends Active
{
    private Instrument petrol;
    private String refillAnimation;
    private String refillEffect;
    private float refillTime;
    private float refillAmount;
    private float workingTimeFrom;
    private float workingTimeTo;

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        petrol = BrainOut.ContentMgr.get(jsonData.getString("petrol"), Instrument.class);

        refillAnimation = jsonData.getString("refill-animation");
        refillEffect = jsonData.getString("refill-effect");
        refillTime = jsonData.getFloat("refill-time");
        refillAmount = jsonData.getFloat("refill-amount");

        workingTimeFrom = jsonData.getFloat("working-time-from");
        workingTimeTo = jsonData.getFloat("working-time-to");
    }

    @Override
    public FreeplayGeneratorData getData(String dimension)
    {
        return new FreeplayGeneratorData(this, dimension);
    }

    public Instrument getPetrol()
    {
        return petrol;
    }

    public String getRefillAnimation()
    {
        return refillAnimation;
    }

    public String getRefillEffect()
    {
        return refillEffect;
    }

    public float getRefillTime()
    {
        return refillTime;
    }

    public float getRefillAmount()
    {
        return refillAmount;
    }

    public float getWorkingTimeFrom()
    {
        return workingTimeFrom;
    }

    public float getWorkingTimeTo()
    {
        return workingTimeTo;
    }
}
