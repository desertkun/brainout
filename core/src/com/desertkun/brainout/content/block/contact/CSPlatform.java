package com.desertkun.brainout.content.block.contact;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.components.IgnorePlatformsComponent;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.bullet.BulletData;
import com.desertkun.brainout.data.components.SimplePhysicsComponentData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;

public class CSPlatform extends ContactShape
{
    @Override
    public boolean isContact(BlockData.ContactPayload payload,
                             float x, float y,
                             Vector2 speed, Vector2 impulse, BlockData blockData, Map map, int blockX, int blockY, Vector2 moveForce, float reduce)
    {

        if (payload instanceof SimplePhysicsComponentData.PhysicsPayload)
        {
            SimplePhysicsComponentData.PhysicsPayload p = ((SimplePhysicsComponentData.PhysicsPayload) payload);

            if (p.activeData.getContent().hasComponent(IgnorePlatformsComponent.class))
            {
                return false;
            }

            if (p.contact != SimplePhysicsComponentData.Contact.bottom)
            {
                return false;
            }

            if (speed != null && speed.y > 0)
            {
                return false;
            }
        }

        if (payload instanceof BulletData)
        {
            return false;
        }

        byte mask = blockData.calculateNeighborMask(
            map, blockX, blockY, Constants.Layers.BLOCK_LAYER_FOREGROUND);

        switch (mask)
        {
            // ╔ (right corner)
            case 2:
            {
                if (x > y)
                {
                    moveForce.x = 0;
                    moveForce.y = x - y;

                    if (speed != null)
                    {
                        if (speed.y < 0)
                        {
                            speed.y *= reduce;
                        }
                    }

                    return true;
                }

                return false;
            }
            //  ╗ (left corner)
            case 8:
            {
                x = 1f - x;

                if (x > y)
                {
                    moveForce.x = 0;
                    moveForce.y = x - y;

                    if (speed != null)
                    {
                        if (speed.y < 0)
                        {
                            speed.y *= reduce;
                        }
                    }

                    return true;
                }

                return false;
            }
        }

        return isContactBox(x, y, speed, impulse, 0, 0, 1f, 1f, moveForce, reduce);
    }

    @Override
    public LaunchData calculateContact(BlockData blockData, Map map, int x, int y, LaunchData launchFrom, LaunchData launchTo, boolean in)
    {
        float angle;

        byte mask = blockData.calculateNeighborMask(
            map, x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

        switch (mask)
        {
            // ╔ (right corner)
            case 2:
            {
                angle = 135;

                break;
            }
            //  ╗ (left corner)
            case 8:
            {
                angle = 45;

                break;
            }
            default:
            {
                return calculateContactBox(blockData, launchFrom, launchTo, in);
            }
        }

        return new PointLaunchData(
            launchFrom.getX(),
            launchFrom.getY(),
            angle,
            launchFrom.getDimension()
        );
    }
}
