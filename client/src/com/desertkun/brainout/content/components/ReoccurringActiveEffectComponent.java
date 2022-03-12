package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.Effect;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveEffectComponentData;
import com.desertkun.brainout.data.components.ReoccurringActiveEffectComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ReoccurringActiveEffectComponent")
public class ReoccurringActiveEffectComponent extends ContentComponent
{
    private EffectSet effect;
    private JsonValue effectValue;
    private float periodFrom;
    private float periodTo;

    public ReoccurringActiveEffectComponent()
    {
        effect = new EffectSet();
    }

    @Override
    public ReoccurringActiveEffectComponentData getComponent(ComponentObject componentObject)
    {
        return new ReoccurringActiveEffectComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        effectValue = jsonData.get("effect");
        periodFrom = jsonData.getFloat("periodFrom");
        periodTo = jsonData.getFloat("periodTo");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        effect.read(effectValue);
        effectValue = null;
    }

    public EffectSet getEffect()
    {
        return effect;
    }

    public float getPeriodFrom()
    {
        return periodFrom;
    }

    public float getPeriodTo()
    {
        return periodTo;
    }
}
