package com.desertkun.brainout.data.components;

import com.desertkun.brainout.content.components.PhysicsContactEffectComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("PhysicsContactEffectComponent")
@ReflectAlias("data.components.PhysicsContactEffectComponentData")
public class PhysicsContactEffectComponentData extends Component<PhysicsContactEffectComponent>
{
    private LaunchData launchData;

    public PhysicsContactEffectComponentData(ComponentObject componentObject,
                                             PhysicsContactEffectComponent contentComponent)
    {
        super(componentObject, contentComponent);
    }

    @Override
    public void init()
    {
        super.init();

        SimplePhysicsComponentData phy = getComponentObject().getComponentWithSubclass(SimplePhysicsComponentData.class);

        if (phy == null)
            return;

        launchData = new LaunchData()
        {
            @Override
            public float getX()
            {
                return phy.getX();
            }

            @Override
            public float getY()
            {
                return phy.getY();
            }

            @Override
            public float getAngle()
            {
                return 0;
            }

            @Override
            public String getDimension()
            {
                return getComponentObject().getDimension();
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
                contact();
                break;
            }
        }

        return false;
    }

    private void contact()
    {
        if (launchData == null)
            return;

        getContentComponent().getEffect().launchEffects(launchData);
    }
}
