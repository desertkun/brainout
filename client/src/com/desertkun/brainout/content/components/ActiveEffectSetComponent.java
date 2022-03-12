package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveEffectSetComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ActiveEffectSetComponent")
public class ActiveEffectSetComponent extends ContentComponent
{
    private EffectSet effects;
    private JsonValue effectsValue;

    @Override
    public ActiveEffectSetComponentData getComponent(ComponentObject componentObject)
    {
        return new ActiveEffectSetComponentData((ActiveData)componentObject, this);
    }

    public ActiveEffectSetComponent()
    {
        effects = new EffectSet();
    }

    @Override
    public void write(Json json)
    {

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

    public EffectSet getEffects()
    {
        return effects;
    }
}
