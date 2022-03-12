package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.components.PhysicsContactEffectComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.PhysicsContactEffectComponent")
public class PhysicsContactEffectComponent extends ContentComponent
{
    private EffectSet effect;
    private JsonValue setValue;

    public PhysicsContactEffectComponent()
    {
        effect = new EffectSet();
    }

    @Override
    public PhysicsContactEffectComponentData getComponent(ComponentObject componentObject)
    {
        return new PhysicsContactEffectComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        setValue = jsonData.get("effect");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (setValue != null)
        {
            effect.read(setValue);
            setValue = null;
        }
    }

    public EffectSet getEffect()
    {
        return effect;
    }
}
