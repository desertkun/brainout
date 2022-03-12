package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.Effect;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveDeathEffectComponentData;
import com.desertkun.brainout.data.components.ActiveEffectComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ActiveDeathEffectComponent")
public class ActiveDeathEffectComponent extends ContentComponent
{
    private EffectSet effect;
    private JsonValue setValue;

    public ActiveDeathEffectComponent()
    {
        this.effect = new EffectSet();
    }

    @Override
    public ActiveDeathEffectComponentData getComponent(ComponentObject componentObject)
    {
        return new ActiveDeathEffectComponentData((ActiveData)componentObject, this);
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
