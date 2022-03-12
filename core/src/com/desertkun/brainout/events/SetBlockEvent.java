package com.desertkun.brainout.events;

import com.desertkun.brainout.data.block.BlockData;

public class SetBlockEvent extends Event
{
    public int x, y, layer;
    public String dimension;
    public BlockData blockData;

    @Override
    public ID getID()
    {
        return ID.setBlock;
    }

    private Event init(int x, int y, int layer, String dimension, BlockData blockData)
    {
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.blockData = blockData;
        this.dimension = dimension;

        return this;
    }

    public static Event obtain(int x, int y, int layer, String dimension, BlockData blockData)
    {
        SetBlockEvent e = obtain(SetBlockEvent.class);
        if (e == null) return null;
        return e.init(x, y, layer, dimension, blockData);
    }

    @Override
    public void reset()
    {
        this.x = 0;
        this.y = 0;
        this.layer = 0;
        this.blockData = null;
        this.dimension = null;
    }
}
