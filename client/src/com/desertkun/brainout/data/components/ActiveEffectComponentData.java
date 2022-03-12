package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.content.components.ActiveEffectComponent;
import com.desertkun.brainout.content.effect.Effect;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ActiveEffectComponent")
@ReflectAlias("data.components.ActiveEffectComponentData")
public class ActiveEffectComponentData extends Component<ActiveEffectComponent>
{
    private final ActiveData activeData;
    private final Vector2 offset;

    private Effect effect;
    private EffectData effectData;

    public ActiveEffectComponentData(ActiveData activeData, ActiveEffectComponent component)
    {
        super(activeData, component);

        this.activeData = activeData;
        this.offset = component.getOffset();
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (effectData != null)
        {
            effectData.update(dt);
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        if (effectData != null)
        {
            effectData.render(batch, context);
        }
    }

    @Override
    public void init()
    {
        super.init();

        this.effect = getContentComponent().getEffect();

        initEffect();
    }

    public void initEffect()
    {
        if (effectData == null && effect != null && effect.isEnabled())
        {
            effectData = effect.getEffect(new LaunchData()
            {
                @Override
                public float getX()
                {
                    return activeData.getX() + offset.x;
                }

                @Override
                public float getY()
                {
                    return activeData.getY() + offset.y;
                }

                @Override
                public float getAngle()
                {
                    return 0;
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
            });

            effectData.init();
        }
    }

    public EffectData updateEffect(Effect effect)
    {
        releaseEffect();
        setEffect(effect);
        initEffect();

        return effectData;
    }

    @Override
    public void release()
    {
        super.release();

        releaseEffect();
    }

    public void releaseEffect()
    {
        if (effectData != null)
        {
            effectData.release();
            effectData = null;
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

    public void setEffect(Effect effect)
    {
        this.effect = effect;
    }
}
