package com.desertkun.brainout.content.block.contact;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.interfaces.LaunchData;

public class CSBlock extends ContactShape
{
    @Override
    public boolean isContact(BlockData.ContactPayload payload, float x, float y,
                             Vector2 speed, Vector2 impulse,
                             BlockData blockData, Map map, int blockX, int blockY, Vector2 moveForce,
                             float reduce)
    {
        return isContactBox(x, y, speed, impulse, 0, 0, 1f, 1f, moveForce, reduce);
    }

    @Override
    public LaunchData calculateContact(BlockData blockData, Map map, int x, int y, LaunchData launchFrom, LaunchData launchTo, boolean in)
    {
        return calculateContactBox(blockData, launchFrom, launchTo, in);
    }
}
