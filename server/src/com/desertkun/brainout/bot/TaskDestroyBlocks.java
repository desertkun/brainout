package com.desertkun.brainout.bot;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.block.BlockData;
import com.desertkun.brainout.utils.RandomValue;

public class TaskDestroyBlocks extends Task
{
    private final RandomValue fireTime;
    private final RandomValue firePauseTime;
    private float timer;
    private final Queue<WayPointMap.BlockCoordinates> blocks;
    private float fireTimer, firePauseTimer;

    public TaskDestroyBlocks(TaskStack stack, Queue<WayPointMap.BlockCoordinates> blocks)
    {
        super(stack);

        this.blocks = blocks;

        this.fireTime = new RandomValue(0.5f, 1.0f);
        this.firePauseTime = new RandomValue(0.25f, 0.5f);
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;
        if (timer > 0)
            return;
        timer = 0.1f;

        getController().stopFollowing();

        if (checkWeapons(false))
            return;

        setAim(true);
        openFire(false);

        for (WayPointMap.BlockCoordinates block : blocks)
        {
            BlockData blockData = getMap().getBlock(block.x, block.y, Constants.Layers.BLOCK_LAYER_FOREGROUND);

            if (blockData == null)
                continue;

            float dst2 = Vector2.dst2(getPlayerData().getX(), getPlayerData().getY(), block.x, block.y);

            float randomness;

            if (dst2 > 8 * 8)
            {
                randomness = 1.5f;
            }
            else if (dst2 > 4 * 4)
            {
                randomness = 0.5f;
            }
            else
            {
                randomness = 0.25f;
            }

            if (!getController().checkVisibility(block.x + 0.5f, block.y + 0.5f, this::checkBlock))
            {
                pop();
                return;
            }

            float targetX = block.x + 0.5f, targetY = block.y + 0.5f + MathUtils.random(-randomness, randomness);

            if (getController().lerpAngle(targetX, targetY))
            {
                fireTimer -= dt;
                if (fireTimer <= 0)
                {
                    if (fireTimer < -firePauseTimer)
                    {
                        resetTimers();

                        openFire(true);
                    }
                    else
                    {
                        openFire(false);
                    }
                }
                else
                {
                    openFire(true);
                }
            }
            else
            {
                openFire(false);
            }

            return;
        }

        openFire(false);
        setAim(false);
        pop();
    }

    private boolean checkBlock(int blockX, int blockY)
    {
        for (WayPointMap.BlockCoordinates coordinates : blocks)
        {
            if (blockX == coordinates.x && blockY == coordinates.y)
            {
                return false;
            }
        }

        return true;
    }

    private void resetTimers()
    {
        this.fireTimer = fireTime.getValue();
        this.firePauseTimer = firePauseTime.getValue();
    }
}
