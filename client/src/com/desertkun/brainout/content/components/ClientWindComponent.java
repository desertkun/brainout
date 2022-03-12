package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.components.ClientWindComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientWindComponent")
public class ClientWindComponent extends ContentComponent
{
    private EffectSet effects;
    private JsonValue effectsSetValue;
    private float extraDistance;

    public ClientWindComponent()
    {
        effects = new EffectSet();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new ClientWindComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        effectsSetValue = jsonData.get("effects");
        extraDistance = jsonData.getFloat("extra-distance", 1.0f);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (effectsSetValue != null)
        {
            effects.read(effectsSetValue);
            effectsSetValue = null;
        }
    }

    public float getExtraDistance()
    {
        return extraDistance;
    }

    public EffectSet getEffects()
    {
        return effects;
    }
}
