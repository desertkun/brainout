package com.desertkun.brainout.content.components;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.FlyByComponentData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.FlyByComponent")
public class FlyByComponent extends ContentComponent
{
    private EffectSet effect;
    private JsonValue effectSetValue;
    private float distance;
    private float slowdown;
    private float slowdownTime;

    public FlyByComponent()
    {
        effect = new EffectSet();
    }

    @Override
    public Component getComponent(ComponentObject componentObject)
    {
        return new FlyByComponentData((BulletData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        effectSetValue = jsonData.get("effect");
        distance = jsonData.getFloat("distance");
        slowdown = jsonData.getFloat("slowdown", 0);
        slowdownTime = jsonData.getFloat("slowdown-time", 0);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        if (effectSetValue != null)
        {
            effect.read(effectSetValue);
            effectSetValue = null;
        }
    }

    public EffectSet getEffect()
    {
        return effect;
    }

    public float getDistance()
    {
        return distance;
    }

    public float getSlowdown()
    {
        return slowdown;
    }

    public float getSlowdownTime()
    {
        return slowdownTime;
    }
}
