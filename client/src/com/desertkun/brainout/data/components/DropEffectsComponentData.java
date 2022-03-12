package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.DropEffectsComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.PhysicsContactEvent;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("DropEffectsComponent")
@ReflectAlias("data.components.DropEffectsComponentData")
public class DropEffectsComponentData extends Component<DropEffectsComponent>
{
    private final LaunchData launchData;

    public DropEffectsComponentData(ActiveData activeData,
                                    DropEffectsComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.launchData = new LaunchData()
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
        };
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

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case physicsContact:
            {
                PhysicsContactEvent e = ((PhysicsContactEvent) event);
                contact(e);

                break;
            }
        }

        return false;
    }

    private void contact(PhysicsContactEvent e)
    {
        if (e.speed.len2() > 8 * 8)
        {
            getContentComponent().getEffects().launchEffects(launchData);
        }
    }
}
