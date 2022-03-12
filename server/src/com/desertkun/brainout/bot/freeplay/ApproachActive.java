package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.bot.EnemyNoticedCallback;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskDestroyBlocks;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.common.msg.server.InventoryItemMovedMsg;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.content.Content;
import com.desertkun.brainout.content.components.AutoConvertConsumable;
import com.desertkun.brainout.content.consumable.ConsumableItem;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;
import com.desertkun.brainout.data.consumable.ConsumableRecord;

public class ApproachActive extends Task
{
    private final ActiveData activeData;
    private final EnemyNoticedCallback enemyNoticedCallback;
    private final float approachDistance;
    private float timer;
    private boolean reachedItem;

    public ApproachActive(TaskStack stack, ActiveData item,
                          EnemyNoticedCallback enemyNoticedCallback,
                          float approachDistance)
    {
        super(stack);

        this.activeData = item;
        this.enemyNoticedCallback = enemyNoticedCallback;
        this.approachDistance = approachDistance;
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.1f;

        if (enemyNoticedCallback != null && checkEnemy())
            return;

        if (reachedItem)
        {
            pop();
            return;
        }

        // we have reached the item
        if ( Vector2.dst2(activeData.getX(), activeData.getY(),
                getPlayerData().getX(), getPlayerData().getY()) < 3.0f * 3.0f)
        {
            pop();
            return;
        }

        followItem();
    }

    private void followItem()
    {
        if (!getController().isFollowing(activeData))
        {
            getController().follow(activeData,
                () -> {
                    reachedItem = true;
                    getController().stopFollowing();
                },
                this::failed,
                this::gotBlocksInOurWay,
                approachDistance);
        }
    }

    private void failed()
    {
        pop();
    }

    private void gotBlocksInOurWay(Queue<WayPointMap.BlockCoordinates> blocks)
    {
        if (getStack().getTasks().size > 0 &&
                getStack().getTasks().last() instanceof TaskDestroyBlocks)
            return;

        pushTask(new TaskDestroyBlocks(getStack(), blocks));
    }

    private boolean checkEnemy()
    {
        ActiveData enemy = checkForEnemies();

        if (enemy != null)
        {
            return enemyNoticedCallback.noticed(getStack(), enemy);
        }

        return false;
    }
}
