package com.desertkun.brainout.content.block.contact;

import com.badlogic.gdx.math.Vector2;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.data.interfaces.LaunchData;
import com.desertkun.brainout.data.interfaces.PointLaunchData;

public class CSTriangle extends ContactShape
{
    @Override
    public LaunchData calculateContact(BlockData blockData, Map map, int x, int y, LaunchData launchFrom, LaunchData launchTo, boolean in)
    {
        float angle;

        byte mask = blockData.calculateNeighborMask(
            map, x, y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

        switch (mask)
        {
            // ╔
            case 6:
            {
                angle = 135;

                break;
            }
            //  ╗
            case 12:
            {
                angle = 45;

                break;
            }
            // ╚
            case 3:
            {
                angle = 225;

                break;
            }
            //  ╝
            case 9:
            {
                angle = 315;

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

    @Override
    public boolean isContact(BlockData.ContactPayload payload,
                             float x, float y, Vector2 speed, Vector2 impulse,
                             BlockData blockData, Map map, int blockX, int blockY, Vector2 moveForce, float reduce)
    {
        byte mask = blockData.calculateNeighborMask(
            map, blockX, blockY, Constants.Layers.BLOCK_LAYER_FOREGROUND);

        switch (mask)
        {
            // ╔
            case 6:
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
            //  ╗
            case 12:
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
            /*
            // ╚
            case 3:
            {
                x = 1f - x;

                if (y > x)
                {
                    moveForce.x = 0;
                    moveForce.y = x - y;

                    if (speed != null)
                    {
                        if (speed.y > 0)
                        {
                            speed.y *= reduce;
                        }
                    }

                    return true;
                }

                return false;
            }
            //  ╝
            case 9:
            {
                if (y > x)
                {
                    moveForce.x = 0;
                    moveForce.y = x - y;

                    if (speed != null)
                    {
                        if (speed.y > 0)
                        {
                            speed.y *= reduce;
                        }
                    }

                    return true;
                }

                return false;
            }
            */
        }

        return isContactBox(x, y, speed, impulse, 0, 0, 1f, 1f, moveForce, reduce);
    }
}
