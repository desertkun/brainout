package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.components.interfaces.UpgradeComponent;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.components.InstrumentEffectsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ReplaceInstrumentEffectsComponent")
public class ReplaceInstrumentEffectsComponent extends ContentComponent implements UpgradeComponent
{
    private EffectSetGroup effects;
    private JsonValue effectsValue;

    public ReplaceInstrumentEffectsComponent()
    {
        effects = new EffectSetGroup();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return null;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        effectsValue = jsonData.get("replace");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        effects.readAll(effectsValue);
        effectsValue = null;
    }

    @Override
    public void upgrade(InstrumentData instrumentData)
    {
        InstrumentEffectsComponentData wec =
            instrumentData.getComponentWithSubclass(InstrumentEffectsComponentData.class);

        if (wec != null)
        {
            wec.getEffects().update(effects);
        }
    }

    @Override
    public boolean pre()
    {
        return false;
    }
}
