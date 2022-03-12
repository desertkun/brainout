package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.FlagChunkComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("FlagChunkComponent")
@ReflectAlias("data.components.FlagChunkComponentData")
public class FlagChunkComponentData extends Component<FlagChunkComponent>
{
    public FlagChunkComponentData(ActiveData activeData,
                                  FlagChunkComponent contentComponent)
    {
        super(activeData, contentComponent);
    }

    @Override
    public void init()
    {
        super.init();

        ActiveData activeData = ((ActiveData) getComponentObject());

        Map map = getMap();
        if (map == null)
            return;

        ChunkData chunkData = map.getChunkAt((int)activeData.getX(), (int)activeData.getY());

        if (chunkData == null)
            return;

        chunkData.setFlag(getContentComponent().getFlag());
    }

    @Override
    public void release()
    {
        super.release();

        ActiveData activeData = ((ActiveData) getComponentObject());

        Map map = getMap();
        if (map == null)
            return;

        ChunkData chunkData = map.getChunkAt((int)activeData.getX(), (int)activeData.getY());

        if (chunkData == null)
            return;

        chunkData.removeFlag(getContentComponent().getFlag());
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
        return false;
    }
}
