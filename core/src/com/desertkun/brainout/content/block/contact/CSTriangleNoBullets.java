package com.desertkun.brainout.content.block.contact;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.content.components.IgnorePlatformsComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;

public class CSTriangleNoBullets extends CSTriangle
{
    @Override
    public boolean isContact(
        BlockData.ContactPayload payload, float x, float y, Vector2 speed,
        Vector2 impulse, BlockData blockData, Map map, int blockX, int blockY,
        Vector2 moveForce, float reduce)
    {
        if (payload instanceof SimplePhysicsComponentData.PhysicsPayload)
        {
            SimplePhysicsComponentData.PhysicsPayload p = ((SimplePhysicsComponentData.PhysicsPayload) payload);

            if (p.activeData.getContent().hasComponent(IgnorePlatformsComponent.class))
            {
                return false;
            }
        }

        if (payload instanceof BulletData)
        {
            return false;
        }

        return super.isContact(payload, x, y, speed, impulse, blockData, map, blockX, blockY, moveForce, reduce);
    }
}
