package com.desertkun.brainout.events;

import com.desertkun.brainout.data.block.BlockData;

public class HandleBlockEvent extends Event
{
    public BlockData blockData;

    @Override
    public ID getID()
    {
        return ID.handleBlock;
    }

    private Event init(BlockData blockData)
    {
        this.blockData = blockData;

        return this;
    }

    public static Event obtain(BlockData blockData)
    {
        HandleBlockEvent e = obtain(HandleBlockEvent.class);
        if (e == null) return null;
        return e.init(blockData);
    }

    @Override
    public void reset()
    {
        blockData = null;
    }
}
