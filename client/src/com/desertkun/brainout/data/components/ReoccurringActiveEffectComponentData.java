package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.content.components.ReoccurringActiveEffectComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.effect.CancellableEffect;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ReoccurringActiveEffectComponent")
@ReflectAlias("data.components.ReoccurringActiveEffectComponentData")
public class ReoccurringActiveEffectComponentData extends Component<ReoccurringActiveEffectComponent>
{
    private final ActiveData activeData;
    private EffectSet effect;
    private Array<EffectData> effects;
    private float timer;

    public ReoccurringActiveEffectComponentData(ActiveData activeData, ReoccurringActiveEffectComponent component)
    {
        super(activeData, component);

        this.activeData = activeData;
        this.effects = new Array<>();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer -= dt;

        if (effects.size == 0)
        {
            if (timer < 0)
            {
                launchEffect();
                setTimer();
            }
        }
        else
        {
            boolean done = true;

            for (EffectData data : effects)
            {
                data.update(dt);
                if (!data.isDone())
                    done = false;
            }

            if (done)
            {
                effects.clear();
            }
        }
    }

    private void setTimer()
    {
        this.timer = MathUtils.random(getContentComponent().getPeriodFrom(), getContentComponent().getPeriodTo());
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        for (EffectData data : effects)
        {
            data.render(batch, context);
        }
    }

    @Override
    public void init()
    {
        super.init();

        this.effect = getContentComponent().getEffect();

        setTimer();
    }

    public void launchEffect()
    {
        if (effects.size == 0 && effect != null)
        {
            effect.launchEffects(new LaunchData()
            {
                @Override
                public float getX()
                {
                    return activeData.getX();
                }

                @Override
                public float getY()
                {
                    return activeData.getY();
                }

                @Override
                public float getAngle()
                {
                    return activeData.getAngle();
                }

                @Override
                public String getDimension()
                {
                    return activeData.getDimension();
                }

                @Override
                public boolean getFlipX()
                {
                    return false;
                }
            }, effects);
        }
    }

    @Override
    public void release()
    {
        super.release();

        releaseEffect();
    }

    public void releaseEffect()
    {
        for (EffectData effectData : effects)
        {
            if (effectData instanceof CancellableEffect)
            {
                ((CancellableEffect) effectData).cancel();
            }
            else
            {
                effectData.release();
            }
        }
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }

    @Override
    public boolean hasRender()
    {
        return true;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }
}
