package com.desertkun.brainout.content.block.contact;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;

public abstract class ContactShape
{
    public abstract boolean isContact(
        BlockData.ContactPayload payload,
        float x, float y, Vector2 speed,
        Vector2 impulse, BlockData blockData,
        Map map, int blockX, int blockY, Vector2 moveForce,
        float reduce);

    public abstract LaunchData calculateContact(
        BlockData blockData, Map map, int x, int y,
        LaunchData launchFrom, LaunchData launchTo, boolean in);

    public boolean isContactBox(float x, float y,
                                Vector2 speed, Vector2 impulse,
                                float bX, float bY, float bW, float bH,
                                Vector2 moveForce,
                                float reduce)
    {
        if (x >= bX && y >= bY && x <= bX + bW && y <= bY + bH)
        {
            if (impulse.x > 0)
            {
                moveForce.x = bX - x;

                if (speed != null)
                {
                    speed.x *= reduce;
                }
            }
            else if (impulse.x < 0)
            {
                moveForce.x = bX + bW - x;

                if (speed != null)
                {
                    speed.x *= reduce;
                }
            }
            else
            {
                moveForce.x = 0;
            }

            if (impulse.y > 0)
            {
                moveForce.y = bY - y;

                if (speed != null)
                {
                    speed.y *= reduce;
                }
            }
            else if (impulse.y < 0)
            {
                moveForce.y = bY + bH - y;

                if (speed != null)
                {
                    speed.y *= reduce;
                }
            }
            else
            {
                moveForce.y = 0;
            }

            return true;
        }

        return false;
    }

    public LaunchData calculateContactBox(BlockData blockData, LaunchData launchFrom, LaunchData launchTo, boolean in)
    {
        float angle = 0;

        int xFrom = (int)launchFrom.getX(), yFrom = (int)launchFrom.getY(),
                xTo = (int)launchTo.getX(), yTo = (int)launchTo.getY();

        if (xTo == xFrom)
        {
            if (yTo > yFrom)
            {
                angle = 90;
            }
            else
            {
                angle = 270;
            }
        }
        else
        {
            if (xTo > xFrom)
            {
                angle = 0;
            }
            else
            {
                angle = 180;
            }
        }

        return new PointLaunchData(
            launchFrom.getX(),
            launchFrom.getY(),
            angle, launchFrom.getDimension()
        );
    }

}
