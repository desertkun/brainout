package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.BlockEffectsComponent;
import com.desertkun.brainout.content.effect.EffectSetGroup;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.LaunchEffectEvent;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("BlockEffectsComponent")
@ReflectAlias("data.components.BlockEffectsComponentData")
public class BlockEffectsComponentData extends Component<BlockEffectsComponent>
{
    public BlockEffectsComponentData(ComponentObject componentObject, BlockEffectsComponent blockEffectsComponent)
    {
        super(componentObject, blockEffectsComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case launchEffect:
            {
                LaunchEffectEvent launchEffectEvent = (LaunchEffectEvent)event;
                EffectSetGroup effects = getContentComponent().getEffects();

                switch (launchEffectEvent.kind)
                {
                    case hit:
                    {
                        effects.launchEffects("hit", launchEffectEvent.launchData);

                        break;
                    }

                    case step:
                    {
                        effects.launchEffects("step", launchEffectEvent.launchData);

                        break;
                    }

                    case destroy:
                    {
                        effects.launchEffects("destroy", launchEffectEvent.launchData);

                        break;
                    }

                    case place:
                    {
                        effects.launchEffects("place", launchEffectEvent.launchData);

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
