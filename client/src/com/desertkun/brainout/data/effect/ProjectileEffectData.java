package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.content.effect.ProjectileEffect;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.graphics.CenterSprite;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.ProjectileEffectData")
public class ProjectileEffectData extends EffectData
{
    private final CenterSprite projectile;
    private float alphaBefore, time;

    public ProjectileEffectData(ProjectileEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        this.projectile = new CenterSprite(effect.getProjectile(), launchData);
        this.alphaBefore = effect.getAlphaBefore();
        this.time = 0;
    }

    @Override
    public void init()
    {
    }

    @Override
    public int getEffectLayer()
    {
        return 2;
    }

    @Override
    public void release()
    {
        super.release();
    }

    @Override
    public void render(Batch spriteBatch, RenderContext context)
    {
        projectile.draw(spriteBatch);
    }

    @Override
    public void update(float dt)
    {
        time += dt;

        if (time < alphaBefore)
        {
            float coef = time / alphaBefore;
            projectile.setColor(1.0f, 1.0f, 1.0f, coef);

            if (time >= alphaBefore)
            {
                projectile.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }

        float t = ((ProjectileEffect) getEffect()).getTtl();
        if (t != 0 && time > t)
        {
            release();
        }
    }

    @Override
    public boolean done()
    {
        return false;
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
