package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.content.effect.ContinuousParticleEffect;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.interfaces.FlippedAngle;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.ContinuousParticleEffectData")
public class ContinuousParticleEffectData extends EffectData implements CancellableEffect
{
    private final ContinuousParticleEffect particleEffect;
    protected com.badlogic.gdx.graphics.g2d.ParticleEffect effect;
    protected float angleMin;
    protected float angleMax;
    protected float rotationMin;
    protected float rotationMax;
    protected ParticleEmitter emitter;
    protected boolean done;
    protected float doneTimer;
    private Vector2 prevPosition;

    public ContinuousParticleEffectData(ContinuousParticleEffect particleEffect, LaunchData launchData)
    {
        super(particleEffect, launchData);

        this.prevPosition = new Vector2();
        this.particleEffect = particleEffect;
    }

    public ContinuousParticleEffectData(ContinuousParticleEffect particleEffect, LaunchData launchData,
                                        EffectSet.EffectAttacher attacher)
    {
        this(particleEffect, launchData);
    }

    @Override
    public int getEffectLayer()
    {
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

        prevPosition.set(getX(), getY());
    }

    public void setHighEmission(float e)
    {
        this.emitter.getEmission().setHigh(e);
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
            emitter.setMaxParticleCount(0);
        }

        effect.reset();
    }

    @Override
    public void update(float dt)
    {
        if (doneTimer > 0)
        {
            doneTimer -= dt;

            if (doneTimer <= 0)
            {
                doneTimer = 0;
                done = true;
            }
        }

        if (particleEffect.getSubsteps() == 1)
        {
            effect.setPosition(getX(), getY());

            ParticleEmitter emitter = effect.getEmitters().first();

            float angle = getAngle();

            emitter.getAngle().setHigh(angleMin + angle, angleMax + angle);
            emitter.getRotation().setHigh(rotationMin + angle, rotationMax + angle);

            effect.update(dt);
        }
        else
        {
            dt /= (float)particleEffect.getSubsteps();

            float x = getX(), y = getY();
            float diffX = (x - prevPosition.x) / (float)particleEffect.getSubsteps(), diffY = (y - prevPosition.y) / (float)particleEffect.getSubsteps();

            for (int i = 0, t = particleEffect.getSubsteps(); i < t; i++)
            {
                effect.setPosition(prevPosition.x + diffX * i, getY() + diffY * i);

                ParticleEmitter emitter = effect.getEmitters().first();

                float angle = getAngle();

                emitter.getAngle().setHigh(angleMin + angle, angleMax + angle);
                emitter.getRotation().setHigh(rotationMin + angle, rotationMax + angle);

                effect.update(dt);
            }

            prevPosition.set(x, y);
        }
    }

    @Override
    public boolean done()
    {
        if (particleEffect.getStickTime() == 0)
        {
            return effect.isComplete();
        }

        return done;
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

    @Override
    public void cancel()
    {
        if (particleEffect.isStopEmitter())
        {
            for (ParticleEmitter effectEmitter : effect.getEmitters())
            {
                effectEmitter.setContinuous(false);
            }
        }

        if (particleEffect.getStickTime() > 0)
        {
            doneTimer = particleEffect.getStickTime();
        }
        else
        {
            done = true;
        }
    }
}
