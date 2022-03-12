package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.content.Team;
import com.desertkun.brainout.content.components.CollisionDetectorComponent;
import com.desertkun.brainout.content.components.EnemyDetectorComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.DetectedEvent;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("CollisionDetectorComponent")
@ReflectAlias("data.components.CollisionDetectorComponentData")
public class CollisionDetectorComponentData extends Component<CollisionDetectorComponent>
{

    private final ActiveData activeData;
    private boolean detected;

    public CollisionDetectorComponentData(ActiveData activeData,
                                          CollisionDetectorComponent detectorComponent)
    {
        super(activeData, detectorComponent);

        this.activeData = activeData;
        this.detected = false;
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
                detect();

                break;
            }
        }

        return false;
    }

    private void detect()
    {
        if (detected)
            return;

        detected = true;

        BrainOutServer.EventMgr.sendDelayedEvent(activeData,
                DetectedEvent.obtain(
                        getContentComponent().getDetectClass(),
                        activeData,
                        DetectedEvent.EventKind.enter));
    }
}
