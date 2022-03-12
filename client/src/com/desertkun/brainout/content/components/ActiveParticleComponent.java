package com.desertkun.brainout.content.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.ParticleEffect;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ActiveParticleComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ActiveParticleComponent")
public class ActiveParticleComponent extends ContentComponent
{
    private ParticleEffect particleEffect;
    private Vector2 offset = new Vector2();

    @Override
    public ActiveParticleComponentData getComponent(ComponentObject componentObject)
    {
        return new ActiveParticleComponentData((ActiveData)componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        particleEffect = ((ParticleEffect) BrainOutClient.ContentMgr.get(jsonData.getString("particle")));

        if (jsonData.has("offset"))
        {
            offset.set(jsonData.get("offset").getFloat("x"), jsonData.get("offset").getFloat("y"));
        }
    }

    public Vector2 getOffset()
    {
        return offset;
    }

    public ParticleEffect getParticleEffect()
    {
        return particleEffect;
    }
}
