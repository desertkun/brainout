package com.desertkun.brainout.data.block;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.content.block.NonContact;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.containers.ChunkData;
import com.desertkun.brainout.data.interfaces.LaunchData;

import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("data.block.NonContactBD")
public class NonContactBD extends BlockData
{
    private final boolean fixture;

    public NonContactBD(NonContact creator)
    {
        super(creator);

        this.fixture = creator.isFixture();
    }

    @Override
    public boolean isContact(
        ContactPayload payload, float x, float y, Vector2 speed,
        Vector2 impulse, Vector2 moveForce, float reduce,
        Map map, int blockX, int blockY)
    {
        return false;
    }

    @Override
    public boolean isFixture()
    {
        return fixture;
    }

    @Override
    public LaunchData calculateContact(
        LaunchData launchFrom, LaunchData launchTo,
        boolean in, Map map, int blockX, int blockY)
    {
        return null;
    }

    @Override
    public boolean isConcrete()
    {
        return false;
    }
}
