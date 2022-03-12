package com.desertkun.brainout.data.components;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.content.components.ExtendedInstrumentEffectsComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.effect.EffectData;
import com.desertkun.brainout.data.effect.StartStopSoundEffectData;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.*;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ExtendedInstrumentEffectsComponent")
@ReflectAlias("data.components.ExtendedInstrumentEffectsComponentData")
public class ExtendedInstrumentEffectsComponentData extends InstrumentEffectsComponentData
{
    private EffectSetGroup effects;
    private Array<EffectData> ext;
    private LaunchData extLaunchData;
    private EffectSet.EffectAttacher extAttacher;
    private boolean active, released;
    private String activeDimension;
    private float shootExtTimer;

    public ExtendedInstrumentEffectsComponentData(ComponentObject componentObject,
                                                  ExtendedInstrumentEffectsComponent instrumentEffectsComponent)
    {
        super(componentObject, instrumentEffectsComponent);

        this.effects = new EffectSetGroup(instrumentEffectsComponent.getEffects());
        this.shootExtTimer = 0;
        this.active = false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case launchAttachedEffect:
            {
                LaunchAttachedEffectEvent e = (LaunchAttachedEffectEvent)event;

                launchEffect(e.kind, e.effectAttacher);

                break;
            }

            case setInstrument:
            {
                SetInstrumentEvent e = ((SetInstrumentEvent) event);

                if (e.playerData == ((InstrumentData) getComponentObject()).getOwner())
                {
                    if (getComponentObject() != e.selected)
                    {
                        deactivate();
                    }
                }
                break;
            }
            case ownerChanged:
            {
                OwnerChangedEvent e = ((OwnerChangedEvent) event);

                if (e.newOwner == null)
                {
                    deactivate();
                }

                break;
            }

            case simple:
            {
                SimpleEvent e = ((SimpleEvent) event);

                if (e.getAction() == SimpleEvent.Action.deselected)
                {
                    deactivate();
                    break;
                }

                break;
            }

            case launchEffect:
            {
                LaunchEffectEvent launchEffectEvent = (LaunchEffectEvent)event;

                if (launchEffectEvent.kind == null)
                    return false;

                switch (launchEffectEvent.kind)
                {
                    case hit:
                    {
                        effects.launchEffects("hit", launchEffectEvent.launchData);

                        return true;
                    }

                    case shoot:
                    {
                        extLaunchData = launchEffectEvent.launchData;
                        extAttacher = null;
                        activeDimension = extLaunchData.getDimension();
                        shootExtTimer =
                            ((ExtendedInstrumentEffectsComponent) getContentComponent()).getExtendedShootPeriod();

                        update(0);

                        return true;
                    }

                    case reload:
                    {
                        effects.launchEffects("reload", launchEffectEvent.launchData);

                        return true;
                    }

                    case fetch:
                    {
                        effects.launchEffects("fetch", launchEffectEvent.launchData);

                        return true;
                    }

                    case switchMode:
                    {
                        effects.launchEffects("switchMode", launchEffectEvent.launchData);

                        return true;
                    }

                    case custom:
                    {
                        effects.launchEffects(launchEffectEvent.custom, launchEffectEvent.launchData);

                        return true;
                    }
                }

                return false;
            }
        }

        return false;
    }

    private void deactivate()
    {
        deactivate(false);
    }

    private void deactivate(boolean forceStop)
    {
        if (activeDimension != null && ext != null)
        {
            ClientMap map = Map.Get(activeDimension, ClientMap.class);

            if (map != null)
            {

                if (forceStop)
                {
                    for (EffectData effectData: ext)
                    {
                        if (effectData instanceof StartStopSoundEffectData)
                        {
                            effectData.release();
                        }
                    }
                }

                map.removeEffect(ext);
            }

            ext.clear();
        }

        active = false;
    }

    public void launchEffect(String kind, EffectSet.EffectAttacher effectAttacher)
    {
        if ("shoot".equals(kind))
        {
            extLaunchData = null;
            extAttacher = effectAttacher;
            shootExtTimer =
                    ((ExtendedInstrumentEffectsComponent) getContentComponent()).getExtendedShootPeriod();

            update(0);
        }

        effects.launchEffects(kind, effectAttacher);
    }

    @Override
    public void update(float dt)
    {
        if (released)
            return;

        if (shootExtTimer > 0)
        {
            shootExtTimer -= dt;

            if (!active)
            {
                if (ext == null)
                    ext = new Array<>();

                if (extLaunchData != null)
                {
                    activeDimension = extLaunchData.getDimension();
                    effects.launchEffects("shootExt", extLaunchData, ext);
                }
                else if (extAttacher != null)
                {
                    ClientMap clientMap = effects.launchEffects("shootExt", extAttacher, ext);
                    if (clientMap != null)
                    {
                        activeDimension = clientMap.getDimension();
                    }
                }
                active = true;
            }
        }
        else
        {
            if (active)
            {
                deactivate();
            }
        }
    }

    @Override
    public void release()
    {
        super.release();

        if (active)
        {
            deactivate(true);

            // no updates for you
            released = true;
        }
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    public EffectSetGroup getEffects()
    {
        return effects;
    }
}
