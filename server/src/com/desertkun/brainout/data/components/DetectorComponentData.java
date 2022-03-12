package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.DetectorComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.DetectedEvent;
import com.desertkun.brainout.events.Event;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("DetectorComponent")
@ReflectAlias("data.components.DetectorComponentData")
public class DetectorComponentData<T extends DetectorComponent> extends Component<T>
{
    private final ActiveData activeData;
    private Array<ActiveData> detected;
    private Rectangle range;
    private float timer;

    public DetectorComponentData(ActiveData activeData, T detectorComponent)
    {
        super(activeData, detectorComponent);

        this.activeData = activeData;
        this.detected = new Array<>();
        this.range = new Rectangle();
        this.timer = 0;
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return true;
    }

    @Override
    public void update(float dt)
    {
        super.update(dt);

        timer -= dt;

        if (timer <= 0)
        {
            timer = getContentComponent().getPeriod();

            detect();
        }
    }

    public ActiveData getMe()
    {
        return activeData;
    }

    private void detect()
    {
        Map map = getMap();

        range.set(activeData.getX() + getContentComponent().getX(),
                activeData.getY() + getContentComponent().getY(),
                getContentComponent().getWidth(),
                getContentComponent().getHeight());

        for (ActiveData activeData : detected)
        {
            if (!test(activeData))
            {
                BrainOutServer.EventMgr.sendDelayedEvent(getComponentObject(),
                        DetectedEvent.obtain(getContentComponent().getDetectClass(),
                            activeData, DetectedEvent.EventKind.leave));

                detected.removeValue(activeData, true);
            }
        }

        for (ActiveData activeData : map.getActivesForTag(Constants.ActiveTags.DETECTABLE, false))
        {
            if (!detected.contains(activeData, true) && test(activeData))
            {
                BrainOutServer.EventMgr.sendDelayedEvent(getComponentObject(),
                    DetectedEvent.obtain(
                        getContentComponent().getDetectClass(),
                        activeData,
                        DetectedEvent.EventKind.enter));

                detected.add(activeData);
            }
        }
    }

    private boolean test(ActiveData activeData)
    {
        if (!activeData.isAlive()) return false;
        if (!activeData.isDetectable()) return false;
        if (activeData.getComponentWithSubclass(SimplePhysicsComponentData.class) == null) return false;
        if (activeData == this.activeData) return false;
        if (!validate(activeData)) return false;

        float pX = activeData.getX(), pY = activeData.getY();

        return range.contains(pX, pY);
    }

    protected boolean validate(ActiveData activeData)
    {
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
