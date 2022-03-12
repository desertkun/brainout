package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.DetectOnActivateInstrumentComponent;
import com.desertkun.brainout.data.ServerMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.instrument.InstrumentData;
import com.desertkun.brainout.events.DetectedEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("DetectOnActivateInstrumentComponent")
@ReflectAlias("data.components.DetectOnActivateInstrumentComponentData")
public class DetectOnActivateInstrumentComponentData extends Component<DetectOnActivateInstrumentComponent>
{
    private final InstrumentData instrumentData;

    public DetectOnActivateInstrumentComponentData(InstrumentData instrumentData,
                                                   DetectOnActivateInstrumentComponent activateComponent)
    {
        super(instrumentData, activateComponent);

        this.instrumentData = instrumentData;
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case instrumentActivate:
            {
                detect();

                break;
            }
        }

        return false;
    }

    private void detect()
    {
        ServerMap map = getMap(ServerMap.class);

        if (map == null)
            return;

        if (instrumentData.getOwner() == null) return;

        for (ActiveData activeData: map.getActivesForTag(Constants.ActiveTags.DETECTORS, false))
        {
            if (activeData.getOwnerId() == instrumentData.getOwner().getOwnerId())
            {
                BrainOutServer.EventMgr.sendDelayedEvent(activeData,
                    DetectedEvent.obtain(
                        getContentComponent().getDetectClass(),
                        activeData,
                        DetectedEvent.EventKind.enter));
            }
        }
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
