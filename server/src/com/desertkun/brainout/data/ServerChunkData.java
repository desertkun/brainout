package com.desertkun.brainout.data;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.data.containers.BlockMatrixData;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.SetDirtyEvent;

public class ServerChunkData extends ChunkData
{
    public ServerChunkData(BlockMatrixData matrixData, int idX, int idY)
    {
        super(matrixData, idX, idY);
    }

    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case setDirty:
            {
                SetDirtyEvent e = ((SetDirtyEvent) event);

                ServerMap map = (ServerMap)getMap();
                map.getWayPointMap().setChunkDirty(idX, idY, e.x, e.y);

                break;
            }
        }

        return super.onEvent(event);
    }
}
