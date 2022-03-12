package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.components.ExtendedInstrumentEffectsComponentData;
import com.desertkun.brainout.data.components.InstrumentEffectsComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ExtendedInstrumentEffectsComponent")
public class ExtendedInstrumentEffectsComponent extends InstrumentEffectsComponent
{
    private float extendedShootPeriod;

    public ExtendedInstrumentEffectsComponent()
    {
        super();

        effects.put("shootExt", new EffectSet());
        extendedShootPeriod = 0;
    }

    @Override
    public ExtendedInstrumentEffectsComponentData getComponent(ComponentObject componentObject)
    {
        return new ExtendedInstrumentEffectsComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        extendedShootPeriod = jsonData.getFloat("shootExtPeriod", extendedShootPeriod);
    }


    public float getExtendedShootPeriod()
    {
        return extendedShootPeriod;
    }

    public EffectSetGroup getEffects()
    {
        return effects;
    }
}
