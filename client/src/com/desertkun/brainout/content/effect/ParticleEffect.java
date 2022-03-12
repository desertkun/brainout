package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.ParticleEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.ParticleEffect")
public class ParticleEffect extends Effect
{
    private com.badlogic.gdx.graphics.g2d.ParticleEffect particle;
    private String particleName;
    private String atlas;
    private boolean limitWhenDone;
    private int layer = -1;

    public ParticleEffect()
    {
    }

    @Override
    public ParticleEffectData getEffect(LaunchData launchData)
    {
        return new ParticleEffectData(this, launchData);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        limitWhenDone = jsonData.getBoolean("limitWhenDone", true);
        particleName = jsonData.getString("particle");
        atlas = jsonData.getString("atlas", "base:textures/GAME.atlas");
        layer = jsonData.getInt("layer", -1);
    }

    public int getLayer()
    {
        return layer;
    }

    @Override
    public void loadContent(AssetManager assetManager)
    {
        super.loadContent(assetManager);

        ParticleEffectLoader.ParticleEffectParameter parameter = new ParticleEffectLoader.ParticleEffectParameter();

        parameter.atlasFile = atlas;

        assetManager.load(particleName, com.badlogic.gdx.graphics.g2d.ParticleEffect.class, parameter);
    }

    @Override
    public void completeLoad(AssetManager assetManager)
    {
        super.completeLoad(assetManager);

        particle = assetManager.get(particleName, com.badlogic.gdx.graphics.g2d.ParticleEffect.class);
    }

    public boolean isLimitWhenDone()
    {
        return limitWhenDone;
    }

    public com.badlogic.gdx.graphics.g2d.ParticleEffect getParticle()
    {
        return particle;
    }
}
