package com.desertkun.brainout.bot;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.mode.GameMode;

public class TaskFollowTargetForever extends Task
{
    private ActiveData followTarget;
    private float timer;

    public TaskFollowTargetForever(
        TaskStack stack,
        ActiveData followTarget)
    {
        super(stack);

        this.followTarget = followTarget;
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.25f;

        if (getController().isFollowing(followTarget))
            return;

        getController().follow(followTarget, this::done, this::stuck, this::gotBlocksInOurWay);
    }

    private void done()
    {

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

}
