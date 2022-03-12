package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.HitPhysicEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.HitPhysicEffect")
public class HitPhysicEffect extends PhysicEffect
{
    private EffectSet onHit;
    private JsonValue onHitValue;

    public HitPhysicEffect()
    {
        super();

        this.onHit = new EffectSet();
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        onHitValue = jsonData.get("onHit");
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        onHit.read(onHitValue);
        onHitValue = null;
    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new HitPhysicEffectData(this, launchData);
    }

    public EffectSet getOnHit()
    {
        return onHit;
    }
}
