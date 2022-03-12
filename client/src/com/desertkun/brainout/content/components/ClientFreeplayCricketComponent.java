package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.SoundEffect;
import com.desertkun.brainout.data.components.ClientFreeplayCricketComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientFreeplayCricketComponent")
public class ClientFreeplayCricketComponent extends ContentComponent
{
    private Array<SoundEffect> effects;
    private float interval;
    private int cycles;
    private float delayAfterPeriod;
    private boolean periodic;

    public ClientFreeplayCricketComponent()
    {
        effects = new Array<>();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ClientFreeplayCricketComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        for (JsonValue value : jsonData.get("effects"))
        {
            effects.add(BrainOutClient.ContentMgr.get(value.asString(), SoundEffect.class));

        }
        interval = jsonData.getFloat("interval");
        delayAfterPeriod = jsonData.getFloat("delayAfterPeriod", 0);
        cycles = jsonData.getInt("cycles", 2);
        periodic = jsonData.getBoolean("periodic", false);
    }

    public boolean isPeriodic()
    {
        return periodic;
    }

    public float getDelayAfterPeriod()
    {
        return delayAfterPeriod;
    }

    public int getCycles()
    {
        return cycles;
    }

    public Array<SoundEffect> getEffects()
    {
        return effects;
    }

    public float getInterval()
    {
        return interval;
    }
}
