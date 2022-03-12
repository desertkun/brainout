package com.desertkun.brainout.events;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.block.BlockData;

public class BlockHitConfirmationEvent extends Event
{
    public ActiveData sender;
    public BlockData block;
    public int dimension;
    public int x, y;
    public int damage;

    @Override
    public void reset()
    {
        sender = null;
        block = null;
        damage = 0;
    }

    private Event init(ActiveData sender, BlockData block, int x, int y, int dimension, int damage)
    {
        this.sender = sender;
        this.block = block;
        this.damage = damage;
        this.x = x;
        this.y = y;
        this.dimension = dimension;
        return this;
    }

    public static Event obtain(ActiveData sender, BlockData block, int x, int y, int dimension, int damage)
    {
        BlockHitConfirmationEvent e = obtain(BlockHitConfirmationEvent.class);
        if (e == null) return null;
        return e.init(sender, block, x, y, dimension, damage);
    }

    @Override
    public ID getID()
    {
        return ID.blockHitConfirmation;
    }
}
