package com.desertkun.brainout.content.block.contact;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ThrowableActiveData;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;

public class CSCombined extends CSBlock
{
    private CSTriangle triangle;

    public CSCombined()
    {
        triangle = new CSTriangle();
    }


    @Override
    public boolean isContact(BlockData.ContactPayload payload, float x, float y,
                             Vector2 speed, Vector2 impulse,
                             BlockData blockData, Map map, int blockX, int blockY, Vector2 moveForce,
                             float reduce)
    {
        if (payload instanceof SimplePhysicsComponentData.PhysicsPayload)
        {
            SimplePhysicsComponentData.PhysicsPayload physicsPayload =
                ((SimplePhysicsComponentData.PhysicsPayload) payload);

            if (physicsPayload.activeData instanceof ThrowableActiveData)
            {
                return super.isContact(payload, x, y, speed, impulse, blockData,
                        map, blockX, blockY, moveForce, reduce);
            }
        }

        if (payload instanceof BulletData)
        {
            return super.isContact(payload, x, y, speed, impulse, blockData,
                    map, blockX, blockY, moveForce, reduce);
        }

        return triangle.isContact(payload, x, y, speed, impulse, blockData,
            map, blockX, blockY, moveForce, reduce);
    }
}
