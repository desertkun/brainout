package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.PlayerEffectsComponent;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.LaunchEffectEvent;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("PlayerEffectsComponent")
@ReflectAlias("data.components.PlayerEffectsComponentData")
public class PlayerEffectsComponentData extends Component<PlayerEffectsComponent>
{
    public PlayerEffectsComponentData(ComponentObject componentObject, PlayerEffectsComponent playerEffectsComponent)
    {
        super(componentObject, playerEffectsComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case launchEffect:
            {
                LaunchEffectEvent launchEffectEvent = (LaunchEffectEvent)event;
                EffectSetGroup effects =  getContentComponent().getEffects();

                switch (launchEffectEvent.kind)
                {
                    case hit:
                    {
                        effects.launchEffects("hit", launchEffectEvent.launchData);

                        break;
                    }
                    case bleeding:
                    {
                        effects.launchEffects("bleeding", launchEffectEvent.launchData);

                        break;
                    }
                    case custom:
                    {
                        effects.launchEffects(launchEffectEvent.custom, launchEffectEvent.launchData);

                        break;
                    }
                }

                break;
            }
        }

        return false;
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
}
