package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.components.BlockEffectsComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BlockEffectsComponent")
public class BlockEffectsComponent extends ContentComponent
{
    private EffectSetGroup effects;
    private JsonValue setValue;

    public BlockEffectsComponent()
    {
        effects = new EffectSetGroup(new String[]{
            "hit",
            "destroy",
            "place",
            "step"
        });
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new BlockEffectsComponentData(componentObject, this);
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
