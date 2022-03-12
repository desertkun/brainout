package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.InstrumentEffectsComponent;
import com.desertkun.brainout.content.effect.EffectSet;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.LaunchAttachedEffectEvent;
import com.desertkun.brainout.events.LaunchEffectEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("InstrumentEffectsComponent")
@ReflectAlias("data.components.InstrumentEffectsComponentData")
public class InstrumentEffectsComponentData extends Component<InstrumentEffectsComponent>
{
    private EffectSetGroup effects;

    public InstrumentEffectsComponentData(ComponentObject componentObject,
                                          InstrumentEffectsComponent instrumentEffectsComponent)
    {
        super(componentObject, instrumentEffectsComponent);

        this.effects = new EffectSetGroup(instrumentEffectsComponent.getEffects());
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
                        effects.launchEffects("shoot", launchEffectEvent.launchData);
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

    public void launchEffect(String kind, EffectSet.EffectAttacher effectAttacher)
    {
        effects.launchEffects(kind, effectAttacher);
    }

    public void launchEffect(String kind, LaunchData launchData)
    {
        effects.launchEffects(kind, launchData);
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
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
