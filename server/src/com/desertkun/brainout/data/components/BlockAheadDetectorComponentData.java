package com.desertkun.brainout.data.components;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.BlockAheadDetectorComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.components.base.ComponentObject;
import com.desertkun.brainout.events.DetectedEvent;
import com.desertkun.brainout.events.Event;

public class BlockAheadDetectorComponentData extends Component<BlockAheadDetectorComponent>
{
    private final ActiveData activeData;
    private boolean detected;

    public BlockAheadDetectorComponentData(ActiveData activeData,
                                           BlockAheadDetectorComponent contentComponent)
    {
        super(activeData, contentComponent);

        this.activeData = activeData;
        this.detected = false;
    }

    @Override
    public void update(float dt)
    {
        if (this.detected)
            return;

        float angle = activeData.getAngle();
        float distance = getContentComponent().getDetectDistance();

        float x = activeData.getX() + MathUtils.cosDeg(angle) * distance,
              y = activeData.getY() + MathUtils.sinDeg(angle) * distance;

        Map map = getMap();

        BlockData block = map.getBlockAt(x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

        if (block != null && block.isConcrete())
        {
            detect();
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
        return true;
    }

    @Override
    public boolean onEvent(Event event)
    {
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
