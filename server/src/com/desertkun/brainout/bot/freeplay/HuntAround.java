package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.bot.*;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.MapDimensionsGraph;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.utils.RandomValue;

import java.util.List;

public class HuntAround extends Task
{
    private final IntSet weaponBlackList;
    private final Array<ActiveData> targets;
    private float timer;
    private int essentialsCheck;
    private ActiveData target;
    private int retries;

    public HuntAround(TaskStack stack, IntSet weaponBlackList)
    {
        super(stack);

        this.weaponBlackList = weaponBlackList;

        Map myMap = getMap();
        targets = new Array<>();

        for (Map map_ : Map.All())
        {
            if (map_.isSafeMap())
                continue;

            if (map_ == myMap || MapDimensionsGraph.IsNeighbor(myMap, map_))
            {
                map_.countActivesForTag(Constants.ActiveTags.SPAWNABLE, activeData ->
                {
                    targets.add(activeData);

                    return true;
                });

                map_.countActivesForTag(Constants.ActiveTags.MARKER, activeData ->
                {
                    targets.add(activeData);

                    return true;
                });
            }
        }

        targets.shuffle();
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.1f;

        essentialsCheck++;

        if (essentialsCheck > 10)
        {
            essentialsCheck = 0;

            if (FindEssentials.EnsureEssentials(this, this.weaponBlackList))
                return;
        }

        ActiveData enemy = checkForEnemies();

        if (enemy != null)
        {
            pushTask(new TaskFollowAndShootTarget(getStack(), enemy,
                new RandomValue(0.3f, 0.8f), new RandomValue(0.6f, 1.3f)));
            return;
        }

        if (BrainOutServer.Controller.getGameMode().isGameActive())
        {
            if (target == null)
            {
                if (targets.size == 0)
                {
                    pop();
                }
                else
                {
                    target = targets.pop();
                }
            }
            else
            {
                if (!getController().isFollowing(target))
                {
                    getController().follow(target,
                        this::pop,
                        this::retarget,
                        this::gotBlocksInOurWay);
                }
            }
        }
    }

    @Override
    public void gotShotFrom(ActiveData shooter)
    {
        pushTask(new TaskShootTargetAndDontMove(getStack(), shooter,
            new RandomValue(0.5f, 1.0f), new RandomValue(0.6f, 1.3f)));
    }

    private void retarget()
    {
        retries++;

        target = null;
        getController().stopFollowing();

        if (targets.size == 0)
        {
            pop();
        }
    }

    private void gotBlocksInOurWay(Queue<WayPointMap.BlockCoordinates> blocks)
    {
        if (getStack().getTasks().size > 0 &&
                getStack().getTasks().last() instanceof TaskDestroyBlocks)
            return;

        pushTask(new TaskDestroyBlocks(getStack(), blocks));
    }
}
