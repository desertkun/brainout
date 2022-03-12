package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.EffectComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.EffectComponent")
public class EffectComponent extends ContentComponent
{
    private EffectSet effectSet;
    private JsonValue effectSetValue;

    public EffectComponent()
    {
        effectSet = new EffectSet();
    }

    @Override
    public EffectComponentData getComponent(ComponentObject componentObject)
    {
        return new EffectComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        effectSetValue = jsonData.get("set");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        effectSet.read(effectSetValue);
        effectSetValue = null;
    }

    public EffectSet getEffectSet()
    {
        return effectSet;
    }
}
