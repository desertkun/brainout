package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.ActiveDeathEffectComponent;
import com.desertkun.brainout.data.ClientMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.PointLaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("ActiveDeathEffectComponent")
@ReflectAlias("data.components.ActiveDeathEffectComponentData")
public class ActiveDeathEffectComponentData extends Component<ActiveDeathEffectComponent>
{
    private final ActiveData activeData;

    public ActiveDeathEffectComponentData(ActiveData activeData, ActiveDeathEffectComponent component)
    {
        super(activeData, component);

        this.activeData = activeData;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case destroy:
            {
                destroy();

                break;
            }
        }

        return false;
    }

    private void destroy()
    {
        getContentComponent().getEffect().launchEffects(
            new PointLaunchData(activeData.getX(), activeData.getY(), 0, activeData.getDimension())
        );
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
