package com.desertkun.brainout.data.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.BrainOutClient;
import com.desertkun.brainout.content.effect.LightEffect;
import com.desertkun.brainout.data.LightEntityData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.effect.LightEffectData")
public class LightEffectData extends EffectData
{
    private final float totalTime;
    private final float timeBefore;
    private final float timeAfter;
    private float time;
    private LightEntityData light;
    private Color color;

    public LightEffectData(LightEffect effect, LaunchData launchData)
    {
        super(effect, launchData);

        this.time = effect.getTime();
        this.totalTime = time;
        this.timeBefore = effect.getTimeBefore();
        this.timeAfter = effect.getTimeAfter();

        this.color = new Color();
        color.set(effect.getColorBefore());
    }

    @Override
    public void release()
    {
        super.release();

        light.dispose();
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {

    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean done()
    {
        return time <= 0;
    }

    @Override
    public void init()
    {
        final LaunchData launchData = getLaunchData();
        LightEffect effect = ((LightEffect) getEffect());

        this.light = new LightEntityData(effect.getLightEntity(), launchData.getDimension())
        {
            @Override
            public float getX()
            {
                return launchData.getX();
            }

            @Override
            public float getY()
            {
                return launchData.getY();
            }
        };

        light.init();
    }

    @Override
    public void update(float dt)
    {
        time -= dt;

        if (time > 0)
        {
            LightEffect effect = ((LightEffect) getEffect());

            light.update();

            if (time > totalTime - timeBefore)
            {
                color.set(effect.getColorBefore());
                color.lerp(effect.getLightEntity().getColor(), (totalTime - time) / timeBefore);
            }
            else if (time < timeAfter)
            {
                color.set(effect.getColorAfter());
                color.lerp(effect.getLightEntity().getColor(), time / timeAfter);
            }
            else
            {
                color.set(effect.getLightEntity().getColor());
            }

            if (light.getLight() != null)
            {
                if (!BrainOutClient.ClientSett.hasSoftShadows())
                {
                    color.a *= 0.4f;
                }
                light.getLight().setColor(color);
            }
        }
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
