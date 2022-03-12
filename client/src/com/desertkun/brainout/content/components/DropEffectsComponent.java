package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.DropEffectsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.DropEffectsComponent")
public class DropEffectsComponent extends ContentComponent
{
    private EffectSet effects;
    private JsonValue effectsValue;

    public DropEffectsComponent()
    {
        this.effects = new EffectSet();
    }

    @Override
    public DropEffectsComponentData getComponent(ComponentObject componentObject)
    {
        return new DropEffectsComponentData((ActiveData) componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    public EffectSet getEffects()
    {
        return effects;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        effectsValue = jsonData.get("effects");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        effects.read(effectsValue);
        effectsValue = null;
    }
}
