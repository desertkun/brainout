package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.desertkun.brainout.content.components.ActiveParticleComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ActiveParticleComponent")
@ReflectAlias("data.components.ActiveParticleComponentData")
public class ActiveParticleComponentData extends Component<ActiveParticleComponent>
{
    private final ActiveData activeData;

    private EffectData particle;

    public ActiveParticleComponentData(ActiveData activeData, ActiveParticleComponent particleComponent)
    {
        super(activeData, particleComponent);

        this.activeData = activeData;
    }

    public EffectData getParticle()
    {
        return particle;
    }

    @Override
    public void init()
    {
        super.init();

        ClientMap map = getMap(ClientMap.class);
        if (map == null)
            return;

        if (getContentComponent().getParticleEffect().isEnabled())
        {
            particle = map.addEffect(getContentComponent().getParticleEffect(), new LaunchData()
            {
                @Override
                public float getX()
                {
                    return activeData.getX() + getContentComponent().getOffset().x;
                }

                @Override
                public float getY()
                {
                    return activeData.getY() + getContentComponent().getOffset().y;
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
        }
    }

    @Override
    public void release()
    {
        super.release();

        if (particle != null)
        {
            particle.release();
            particle = null;
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
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }
}
