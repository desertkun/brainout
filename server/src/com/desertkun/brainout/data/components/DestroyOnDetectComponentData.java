package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.DestroyOnDetectComponent;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.data.interfaces.WithTag;
import com.desertkun.brainout.events.DestroyEvent;
import com.desertkun.brainout.events.DetectedEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("DestroyOnDetectComponent")
@ReflectAlias("data.components.DestroyOnDetectComponentData")
public class DestroyOnDetectComponentData extends Component<DestroyOnDetectComponent> implements WithTag
{
    public DestroyOnDetectComponentData(ComponentObject componentObject,
                                        DestroyOnDetectComponent detectComponent)
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
                        destroy();
                    }
                }

                break;
            }
        }

        return false;
    }

    private void destroy()
    {
        BrainOutServer.EventMgr.sendDelayedEvent(getComponentObject(),
            DestroyEvent.obtain());
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
