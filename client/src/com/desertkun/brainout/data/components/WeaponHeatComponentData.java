package com.desertkun.brainout.data.components;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.desertkun.brainout.content.components.WeaponHeatComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.effect.ParticleEffectData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.RenderContext;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SetInstrumentEvent;
import com.desertkun.brainout.events.SimpleEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("WeaponHeatComponent")
@ReflectAlias("data.components.WeaponHeatComponentData")
public class WeaponHeatComponentData extends Component<WeaponHeatComponent>
{
    private ParticleEffectData effectData;
    private float value;

    public WeaponHeatComponentData(ComponentObject componentObject, WeaponHeatComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public void init()
    {
        super.init();

        ClientWeaponComponentData cw = getComponentObject().getComponent(ClientWeaponComponentData.class);

        if (cw != null)
        {
            LaunchData lp = cw.getAttacher().attachTo(getContentComponent().getAttachBone());

            if (getContentComponent().getEffect().isEnabled())
            {
                effectData = getContentComponent().getEffect().getEffect(new LaunchData()
                {
                    @Override
                    public float getX()
                    {
                        return lp.getX();
                    }

                    @Override
                    public float getY()
                    {
                        return lp.getY();
                    }

                    @Override
                    public float getAngle()
                    {
                        return 0;
                    }

                    @Override
                    public String getDimension()
                    {
                        return lp.getDimension();
                    }

                    @Override
                    public boolean getFlipX()
                    {
                        return false;
                    }
                });

                effectData.init();
                effectData.setHighEmission(0);
            }
        }
    }

    @Override
    public void render(Batch batch, RenderContext context)
    {
        super.render(batch, context);

        if (effectData != null && value > 0)
            effectData.render(batch, context);
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        if (effectData != null)
        {
            if (value > 0)
            {
                effectData.setHighEmission(value);
                effectData.update(dt);

                value -= getContentComponent().getAttenuation() * dt;

                if (value < 0)
                    value = 0;
            }
        }
    }

    @Override
    public void release()
    {
        super.release();

        if (effectData != null)
            effectData.release();
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

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case setInstrument:
            {
                SetInstrumentEvent e = ((SetInstrumentEvent) event);

                if (e.playerData == ((InstrumentData) getComponentObject()).getOwner())
                {
                    if (getComponentObject() != e.selected && effectData != null)
                    {
                        effectData.clear();
                    }
                }
                break;
            }
            case simple:
            {
                SimpleEvent e = ((SimpleEvent) event);

                if (e.getAction() == SimpleEvent.Action.deselected)
                {
                    effectData.clear();
                    break;
                }

                break;
            }
            case launchBullet:
            {
                value = Math.min(value + getContentComponent().getLaunchAdd(), getContentComponent().getMax());

                break;
            }
        }

        return false;
    }
}
