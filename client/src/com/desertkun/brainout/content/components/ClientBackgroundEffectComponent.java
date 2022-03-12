package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.ParticleEffect;
import com.desertkun.brainout.data.components.ClientBackgroundEffectComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.ClientBackgroundEffectComponent")
public class ClientBackgroundEffectComponent extends ContentComponent
{
    private ParticleEffect particleEffect;

    @Override
    public ClientBackgroundEffectComponentData getComponent(ComponentObject componentObject)
    {
        return new ClientBackgroundEffectComponentData(componentObject, this);
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        particleEffect = (ParticleEffect) BrainOutClient.ContentMgr.get(jsonData.getString("particle"));
    }

    public ParticleEffect getParticleEffect()
    {
        return particleEffect;
    }
}
