package com.desertkun.brainout.content.effect;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.desertkun.brainout.data.effect.ContinuousParticleEffectData;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.ParticleEffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.effect.ContinuousParticleEffect")
public class ContinuousParticleEffect extends Effect
{
    private com.badlogic.gdx.graphics.g2d.ParticleEffect particle;
    private String particleName;
    private String atlas;
    private float stickTime;
    private int substeps;
    private boolean stopEmitter;

    public ContinuousParticleEffect()
    {
    }

    @Override
    public ContinuousParticleEffectData getEffect(LaunchData launchData)
    {
        return new ContinuousParticleEffectData(this, launchData);
    }

    @Override
    public EffectData getEffect(LaunchData launchData, EffectSet.EffectAttacher attacher)
    {
        return new ContinuousParticleEffectData(this, launchData, attacher);
    }

    @Override
    public void read(Json json, JsonValue jsonData)
    {
        super.read(json, jsonData);

        stickTime = jsonData.getFloat("stickTime");
        substeps = jsonData.getInt("substeps", 1);
        stopEmitter = jsonData.getBoolean("stopEmitter", false);
        particleName = jsonData.getString("particle");
        atlas = jsonData.getString("atlas", "base:textures/GAME.atlas");
    }

    public boolean isStopEmitter()
    {
        return stopEmitter;
    }

    public int getSubsteps()
    {
        return substeps;
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

    public com.badlogic.gdx.graphics.g2d.ParticleEffect getParticle()
    {
        return particle;
    }

    public float getStickTime()
    {
        return stickTime;
    }
}
