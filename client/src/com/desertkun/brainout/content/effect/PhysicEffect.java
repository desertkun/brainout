package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.PhysicEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;
import com.desertkun.brainout.utils.RandomValue;

@Reflect("content.effect.PhysicEffect")
public class PhysicEffect extends Effect
{
    private EffectSet attached;
    private JsonValue attachedValue;

    private RandomValue mass;
    private RandomValue angleOffset;
    private RandomValue speed;
    private RandomValue spring;
    private RandomValue offsetX;
    private RandomValue offsetY;
    private float friction;
    private float reduce;
    private float distanceOffset;
    private boolean removeChild;

    public PhysicEffect()
    {
        this.attached = new EffectSet();
        this.mass = new RandomValue(0.f, 0.f);
        this.angleOffset = new RandomValue(0.f, 0.f);
        this.speed = new RandomValue(0.f, 0.f);
        this.spring = new RandomValue(0.f, 0.f);
        this.distanceOffset = 0;
        this.removeChild = false;
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        attachedValue = jsonData.get("attached");
        mass.read(json, jsonData.get("mass"));
        angleOffset.read(json, jsonData.get("angleOffset"));
        speed.read(json, jsonData.get("speed"));
        spring.read(json, jsonData.get("spring"));

        if (jsonData.has("offsetX"))
        {
            offsetX = new RandomValue(0.0f, 0.0f);
            offsetX.read(json, jsonData.get("offsetX"));
        }

        if (jsonData.has("offsetY"))
        {
            offsetY = new RandomValue(0.0f, 0.0f);
            offsetY.read(json, jsonData.get("offsetY"));
        }

        friction = jsonData.getFloat("friction");
        distanceOffset = jsonData.getFloat("distanceOffset", 0);
        removeChild = jsonData.getBoolean("removeChild", false);
        reduce = jsonData.getFloat("reduce", Constants.Core.PHY_COLLISION_REDUCING);
    }

    public boolean isRemoveChild()
    {
        return removeChild;
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        attached.read(attachedValue);
        attachedValue = null;
    }

    public float getDistanceOffset()
    {
        return distanceOffset;
    }

    public RandomValue getOffsetX()
    {
        return offsetX;
    }

    public RandomValue getOffsetY()
    {
        return offsetY;
    }

    @Override
    public EffectData getEffect(LaunchData launchData)
    {
        return new PhysicEffectData(this, launchData);
    }

    public EffectSet getAttached()
    {
        return attached;
    }

    public float getMass()
    {
        return mass.getValue();
    }

    public float getAngleOffset()
    {
        return angleOffset.getValue();
    }

    public float getSpeed()
    {
        return speed.getValue();
    }

    public float getSpring()
    {
        return spring.getValue();
    }

    public float getFriction()
    {
        return friction;
    }

    public float getReduce()
    {
        return reduce;
    }
}
