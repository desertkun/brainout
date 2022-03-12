package com.desertkun.brainout.bot;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.SpawnTarget;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.BotControllerComponentData;
import com.desertkun.brainout.data.components.ServerTeamVisibilityComponentData;
import com.desertkun.brainout.data.components.SubPointComponentData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.utils.RandomValue;

public class TaskHoldAFlag extends Task
{
    private Vector2 tmp;
    private final FlagData flagData;
    private float timer, complete;
    private boolean reachedSubPoint;
    private float changeAngleCounter;

    public TaskHoldAFlag(TaskStack stack, FlagData flagData)
    {
        super(stack);

        this.tmp = new Vector2();
        this.flagData = flagData;
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.1f;

        if (flagData.getTeam() != getPlayerData().getTeam() || flagData.getState() != FlagData.State.normal)
        {
            setState(Player.State.normal);
            // they're stealing our flag, attack it
            popMeAndPushTask(new TaskAssaultAFlag(getStack(), flagData));
            return;
        }

        if (checkEnemy())
            return;

        if (checkExistingFollowing())
            return;

        // we have reached some subpoint, now we just have to hold on point
        if (reachedSubPoint && Vector2.dst2(flagData.getX(), flagData.getY(),
            getPlayerData().getX(), getPlayerData().getY()) <
            flagData.getSpawnRange() * flagData.getSpawnRange())
        {
            holdAPoint(0.1f);
            return;
        }

        followSubPoint();
    }

    private void holdAPoint(float dt)
    {
        complete += dt;

        if (complete > 4)
        {
            setState(Player.State.normal);
            pop();
            return;
        }

        setAim(true);
        setState(Player.State.sit);

        changeAngleCounter -= dt;
        if (changeAngleCounter < 0)
        {
            changeAngleCounter = MathUtils.random(0.25f, 1.0f);

            tmp.set(flagData.getX(), flagData.getY()).sub(getPlayerData().getX(), getPlayerData().getY());

            float angle = tmp.angleDeg() + MathUtils.random(-10.f, 10.f);
            getPlayerData().setAngle(angle);
        }

        Array<ActiveData> a = getMap().getActivesForTag(
            Constants.ActiveTags.PLAYERS, target ->
        {
            int owner = target.getOwnerId();

            if (owner < 0 || owner == getPlayerData().getOwnerId())
                return false;

            if (!BrainOutServer.Controller.isEnemies(owner, getPlayerData().getOwnerId()))
                return false;

            ServerTeamVisibilityComponentData cmp =
                    target.getComponent(ServerTeamVisibilityComponentData.class);

            if (cmp == null)
                return false;

            return cmp.isVisibleTo(getPlayerData().getOwnerId());
        });

        if (a.size > 0)
        {
            ActiveData activeData = a.random();

            popMeAndPushTask(new TaskFollowAndShootTarget(getStack(), activeData,
                new RandomValue(0.1f, 0.6f), new RandomValue(0.6f, 0.13f))
            {
                @Override
                protected void stuck()
                {
                    pop();
                }
            });
        }
    }

    private void followSubPoint()
    {
        Array<ActiveData> a = getMap().getActivesForTag(
            Constants.ActiveTags.SPAWNABLE, activeData ->
        {
            if (Vector2.dst2(flagData.getX(), flagData.getY(),
                    activeData.getX(), activeData.getY()) >
                    flagData.getSpawnRange() * flagData.getSpawnRange())
                return false;

            SubPointComponentData sp = activeData.getComponent(SubPointComponentData.class);
            if (sp == null)
                return false;

            if (sp.getTarget() != SpawnTarget.flag)
                return false;

            return getController().checkVisibility(activeData.getX(), activeData.getY());
        });

        if (a.size == 0)
        {
            getController().follow(flagData,
                () ->
                {
                    reachedSubPoint = true;
                    getController().stopFollowing();
                },
                getController()::stopFollowing,
                this::gotBlocksInOurWay);

            return;
        }

        getController().follow(a.random(),
        () -> {
            reachedSubPoint = true;
            getController().stopFollowing();
        },
        getController()::stopFollowing,
        this::gotBlocksInOurWay);
    }

    private void gotBlocksInOurWay(Queue<WayPointMap.BlockCoordinates> blocks)
    {
        if (getStack().getTasks().size > 0 &&
            getStack().getTasks().last() instanceof TaskDestroyBlocks)
            return;

        pushTask(new TaskDestroyBlocks(getStack(), blocks));
    }

    private boolean checkExistingFollowing()
    {
        ActiveData followTarget = getController().getFollowTarget();
        if (followTarget == null || followTarget == flagData)
            return false;

        SubPointComponentData sp = followTarget.getComponent(SubPointComponentData.class);
        if (sp == null)
            return false;

        if (Vector2.dst2(flagData.getX(), flagData.getY(),
            followTarget.getX(), followTarget.getY()) >
            flagData.getSpawnRange() * flagData.getSpawnRange())
            return false;

        return true;
    }

    private boolean checkEnemy()
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        float maxDistance = 64f;

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
            pushTask(new TaskShootTarget(getStack(), a.random(),
                new RandomValue(0.5f, 1.0f), new RandomValue(0.1f, 0.5f)));

            return true;
        }

        return false;
    }
}
