package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.content.components.ActiveEffectSetComponent;
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

@Reflect("ActiveEffectSetComponent")
@ReflectAlias("data.components.ActiveEffectSetComponentData")
public class ActiveEffectSetComponentData extends Component<ActiveEffectSetComponent>
{
    private final ActiveData activeData;

    private EffectSet effect;
    private Array<EffectData> effects;

    public ActiveEffectSetComponentData(ActiveData activeData, ActiveEffectSetComponent component)
    {
        super(activeData, component);

        this.activeData = activeData;
        this.effects = new Array<>();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        for (EffectData data : effects)
        {
            data.update(dt);
        }
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

        this.effect = getContentComponent().getEffects();

        initEffect();
    }

    public void initEffect()
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
