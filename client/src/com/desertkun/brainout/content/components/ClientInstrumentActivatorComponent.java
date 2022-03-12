package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.components.ClientInstrumentActivatorComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.instrument.InstrumentData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientInstrumentActivatorComponent")
public class ClientInstrumentActivatorComponent extends ContentComponent
{
    private EffectSet activateEffect;
    private JsonValue activateEffectValue;

    public ClientInstrumentActivatorComponent()
    {
        this.activateEffect = new EffectSet();
    }

    @Override
    public ClientInstrumentActivatorComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientInstrumentActivatorComponentData((InstrumentData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        activateEffectValue = jsonData.get("activateEffect");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        activateEffect.read(activateEffectValue);
        activateEffectValue = null;
    }

    public EffectSet getActivateEffect()
    {
        return activateEffect;
    }
}
