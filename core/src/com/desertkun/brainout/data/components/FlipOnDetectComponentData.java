package com.desertkun.brainout.data.components;

import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.FlipOnDetectComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.events.DetectedEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("FlipOnDetectComponent")
@ReflectAlias("data.components.FlipOnDetectComponentData")
public class FlipOnDetectComponentData extends Component<FlipOnDetectComponent> implements WithTag
{
    public FlipOnDetectComponentData(ComponentObject componentObject,
                                     FlipOnDetectComponent detectComponent)
    {
        super(componentObject, detectComponent);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case detected:
            {
                DetectedEvent detectedEvent = ((DetectedEvent) event);

                if (detectedEvent.eventKind == DetectedEvent.EventKind.enter)
                {
                    if (getContentComponent().getDetectClasses().indexOf(detectedEvent.detectClass, false) >= 0)
                    {
                        flip();
                    }
                }

                break;
            }
        }

        return false;
    }

    private void flip()
    {
        SimplePhysicsComponentData phy = getComponentObject().getComponentWithSubclass(SimplePhysicsComponentData.class);

        phy.getSpeed().scl(-1);
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
    public int getTags()
    {
        return WithTag.TAG(Constants.ActiveTags.DETECTORS);
    }
}
