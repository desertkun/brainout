package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.content.effect.ParticleEffect;
import com.desertkun.brainout.data.interfaces.FlippedAngle;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.ParticleEffectData")
public class ParticleEffectData extends EffectData
{
    private final ParticleEffect particleEffect;
    protected com.badlogic.gdx.graphics.g2d.ParticleEffect effect;
    protected float angleMin;
    protected float angleMax;
    protected float rotationMin;
    protected float rotationMax;
    protected ParticleEmitter emitter;

    public ParticleEffectData(ParticleEffect particleEffect, LaunchData launchData)
    {
        super(particleEffect, launchData);

        this.particleEffect = particleEffect;
    }

    @Override
    public int getEffectLayer()
    {
        if (particleEffect.getLayer() != -1)
        {
            return particleEffect.getLayer();
        }

        return particleEffect.getParticle().getEmitters().get(0).isBehind() ? 0 : 1;
    }

    protected com.badlogic.gdx.graphics.g2d.ParticleEffect newEmitter(com.badlogic.gdx.graphics.g2d.ParticleEffect loadEffect)
    {
        return new com.badlogic.gdx.graphics.g2d.ParticleEffect(loadEffect);
    }

    @Override
    public void init()
    {
        this.effect = newEmitter(particleEffect.getParticle());
        this.emitter = this.effect.getEmitters().first();

        angleMin = emitter.getAngle().getHighMin();
        angleMax = emitter.getAngle().getHighMax();

        rotationMin = emitter.getRotation().getHighMin();
        rotationMax = emitter.getRotation().getHighMax();

        effect.start();
    }

    public void setHighEmission(float e)
    {
        this.emitter.getEmission().setHigh(e);
    }

    public void setHorizontalWind(float w)
    {
        this.emitter.getWind().setHigh(w);
    }

    public void clear()
    {
        this.effect.reset();
    }

    @Override
    public void release()
    {
        super.release();

        for (ParticleEmitter emitter : effect.getEmitters())
        {
            emitter.setContinuous(false);

            if (particleEffect.isLimitWhenDone())
            {
                emitter.setMaxParticleCount(0);
            }
        }

        if (particleEffect.isLimitWhenDone())
        {
            effect.reset();
        }
    }

    @Override
    public void update(float dt)
    {
        effect.setPosition(getX(), getY());

        float angle = getAngle();

        for (ParticleEmitter emitter : effect.getEmitters())
        {
            emitter.getAngle().setHigh(angleMin + angle, angleMax + angle);
            emitter.getRotation().setHigh(rotationMin + angle, rotationMax + angle);
        }

        effect.update(dt);
    }

    @Override
    public boolean done()
    {
        return effect.isComplete();
    }

    @Override
    public void render(Batch spriteBatch, RenderContext context)
    {
        effect.draw(spriteBatch);
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }
}
