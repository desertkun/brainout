package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.components.InstrumentEffectsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.InstrumentEffectsComponent")
public class InstrumentEffectsComponent extends ContentComponent
{
    protected EffectSetGroup effects;
    private JsonValue setValue;

    public InstrumentEffectsComponent()
    {
        effects = new EffectSetGroup(new String[]{
            "shoot",
            "pull",
            "reload",
            "fetch",
            "fetchSecondary",
            "reloadSecondary",
            "switchMode",
            "cock",
            "hit",
            "buildUp",
            "addRound"
        });
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new InstrumentEffectsComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        setValue = jsonData;
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (setValue != null)
        {
            effects.read(setValue);
            setValue = null;
        }
    }

    public EffectSetGroup getEffects()
    {
        return effects;
    }
}
