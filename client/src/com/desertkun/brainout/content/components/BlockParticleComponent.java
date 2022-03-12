package com.desertkun.brainout.content.components;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.components.base.ContentComponent;
import com.desertkun.brainout.content.effect.ParticleEffect;
import com.desertkun.brainout.data.components.BlockParticleComponentData;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.components.BlockParticleComponent")
public class BlockParticleComponent extends ContentComponent
{
    private ParticleEffect particleEffect;

    @Override
    public BlockParticleComponentData getComponent(ComponentObject componentObject)
    {
        return new BlockParticleComponentData(componentObject, this);
    }

    public ParticleEffect getParticleEffect()
    {
        return particleEffect;
    }

    @Override
    public void write(Json json)
    {

    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        particleEffect = BrainOutClient.ContentMgr.get(jsonData.getString("effect"), ParticleEffect.class);
    }
}
