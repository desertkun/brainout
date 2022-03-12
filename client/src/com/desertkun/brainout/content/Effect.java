package com.desertkun.brainout.content;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.effect.EffectSet;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.Effect")
public class Effect extends Content
{
    private EffectSet set;
    private JsonValue setValue;

    public Effect()
    {
        set = new EffectSet();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        setValue = jsonData.get("set");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (setValue != null)
        {
            set.read(setValue);
            setValue = null;
        }
    }

    public EffectSet getSet()
    {
        return set;
    }
}
