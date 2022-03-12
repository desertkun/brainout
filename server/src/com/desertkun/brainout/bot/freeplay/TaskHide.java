package com.desertkun.brainout.bot.freeplay;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.active.PortalData;
import com.desertkun.brainout.data.components.ServerPortalComponentData;
import com.desertkun.brainout.utils.Pair;
import com.desertkun.brainout.utils.RandomValue;

import java.util.Comparator;

public class TaskHide extends Task
{
    private final IntSet weaponBlackList;
    private final boolean engageEnemies;
    private float timer;
    private ActiveData target;
    private int retries;
    private boolean complete, checkOnce;

    public TaskHide(TaskStack stack, IntSet weaponBlackList)
    {
        this(stack, weaponBlackList, true);
    }

    public TaskHide(TaskStack stack, IntSet weaponBlackList, boolean engageEnemies)
    {
        super(stack);

        this.weaponBlackList = weaponBlackList;
        this.checkOnce = true;
        this.engageEnemies = engageEnemies;
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.1f;

        if (this.weaponBlackList != null && FindEssentials.EnsureEssentials(this, this.weaponBlackList))
            return;

        Map map = getMap();
        if (map == null)
            return;

        if (checkOnce)
        {
            checkOnce = false;

            if (!map.getDimension().equals("default") && !map.getDimension().equals("forest") && !map.getDimension().equals("swamp2"))
            {
                popMeAndPushTask(new TaskWait(getStack(), MathUtils.random(3.0f, 5.0f)));
                return;
            }
        }

        if (engageEnemies)
        {
            ActiveData enemy = checkForEnemies();

            if (enemy != null)
            {
                pushTask(new TaskFollowAndShootTarget(getStack(), enemy,
                        new RandomValue(0.3f, 0.8f), new RandomValue(0.3f, 1.0f)));
                return;
            }
        }

        if (complete)
            return;

        if (BrainOutServer.Controller.getGameMode().isGameActive())
        {
            if (target == null)
            {
                pickTarget();
            }
            else
            {
                getController().follow(target,
                    this::complete,
                    this::retarget,
                    this::gotBlocksInOurWay);
            }
        }
    }

    private void complete()
    {
        complete = true;
        target = null;
        popMeAndPushTask(new TaskWait(getStack(), MathUtils.random(3.0f, 5.f)));
    }

    private void retarget()
    {
        retries++;

        target = null;
        getController().stopFollowing();

        if (retries > 10)
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

    private void pickTarget()
    {
        PlayerData playerData = getPlayerData();
        Array<Pair<PortalData, ActiveData>> targets = new Array<>();
        Map myMap = getMap();

        myMap.countActivesForTag(Constants.ActiveTags.PORTAL, activeData ->
        {
            PortalData portalData = ((PortalData) activeData);
            ServerPortalComponentData p = portalData.getComponent(ServerPortalComponentData.class);
            if (p == null)
                return false;
            
            PortalData other = p.findOtherPortal();

            if (other != null)
            {
                Map otherMap = other.getMap();
                if (otherMap == null)
                    return false;

                if (otherMap.getDimension().equals("default") || otherMap.getDimension().equals("forest") || otherMap.getDimension().equals("swamp2"))
                    return false;

                ActiveData spawnAt = other.getMap().getRandomActiveForTag(Constants.ActiveTags.ITEM);

                if (spawnAt != null)
                {
                    targets.add(new Pair<>(portalData, spawnAt));
                }
            }

            return true;
        });

        targets.sort((o1, o2) ->
        {
            float d1 = Vector2.dst2(o1.first.getX(), o1.first.getY(), playerData.getX(), playerData.getY());
            float d2 = Vector2.dst2(o2.first.getX(), o2.first.getY(), playerData.getX(), playerData.getY());

            return (int)(d1 - d2);
        });

        if (targets.size > 0)
        {
            Pair<PortalData, ActiveData> first = targets.first();
            target = first.second;
            return;
        }

        pop();
    }
}
