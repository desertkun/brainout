package com.desertkun.brainout.events;

import com.desertkun.brainout.data.physics.PhysicChunk;

public class PhysicChunkUpdatedEvent extends Event
{
    public PhysicChunk physicChunk;

    @Override
    public ID getID()
    {
        return ID.physicsUpdated;
    }

    private Event init(PhysicChunk physicChunk)
    {
        this.physicChunk = physicChunk;

        return this;
    }

    public static Event obtain(PhysicChunk physicChunk)
    {
        PhysicChunkUpdatedEvent e = obtain(PhysicChunkUpdatedEvent.class);
        if (e == null) return null;
        return e.init(physicChunk);
    }

    @Override
    public void reset()
    {
        this.physicChunk = null;
    }
}
