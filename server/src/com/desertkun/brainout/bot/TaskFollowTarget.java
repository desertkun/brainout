package com.desertkun.brainout.bot;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.mode.GameMode;

public class TaskFollowTarget extends Task
{
    private ActiveData followTarget;
    private boolean autoPop;
    private float timer;
    private float targetDistance;
    private EnemyNoticedCallback enemyNoticedCallback;

    public TaskFollowTarget(
        TaskStack stack,
        ActiveData followTarget,
        EnemyNoticedCallback enemyNoticedCallback,
        boolean autoPop)
    {
        super(stack);

        this.followTarget = followTarget;
        this.enemyNoticedCallback = enemyNoticedCallback;
        this.autoPop = autoPop;
        this.targetDistance = 2.0f;
    }

    public TaskFollowTarget(
        TaskStack stack,
        ActiveData followTarget,
        EnemyNoticedCallback enemyNoticedCallback,
        boolean autoPop, float targetDistance)
    {
        super(stack);

        this.followTarget = followTarget;
        this.enemyNoticedCallback = enemyNoticedCallback;
        this.autoPop = autoPop;
        this.targetDistance = targetDistance;
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.25f;

        if (enemyNoticedCallback != null)
            if (checkEnemy())
                return;

        if (getController().isFollowing(followTarget))
            return;

        getController().follow(followTarget, this::done, this::stuck, this::gotBlocksInOurWay, this.targetDistance);
    }

    private void done()
    {
        if (autoPop)
        {
            pop();
        }
        else
        {
            getController().stopFollowing();
        }
    }

    private void gotBlocksInOurWay(Queue<WayPointMap.BlockCoordinates> blocks)
    {
        if (getStack().getTasks().size > 0 &&
            getStack().getTasks().last() instanceof TaskDestroyBlocks)
            return;

        pushTask(new TaskDestroyBlocks(getStack(), blocks));
    }

    protected void stuck()
    {

    }

    private boolean checkEnemy()
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        float maxDistance = 32f;

        Array<ActiveData> a = getMap().getActivesForTag(Constants.ActiveTags.PLAYERS,
            activeData ->
        {
            if (activeData == getPlayerData())
                return false;

            if (!(activeData instanceof PlayerData))
                return false;

            if (activeData.getOwnerId() >= 0 && getPlayerData().getOwnerId() >= 0)
            {
                if (!gameMode.isEnemies(activeData.getOwnerId(), getPlayerData().getOwnerId()))
                    return false;
            }
            else
            {
                if (!gameMode.isEnemies(activeData.getTeam(), getPlayerData().getTeam()))
                    return false;
            }

            return getController().checkVisibility(activeData, maxDistance, null);
        });

        if (a.size > 0)
        {
            if (enemyNoticedCallback.noticed(getStack(), a.first()))
            {
                pop();
            }

            return true;
        }

        return false;
    }
}
