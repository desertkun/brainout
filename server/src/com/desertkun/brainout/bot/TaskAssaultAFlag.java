package com.desertkun.brainout.bot;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.content.components.ServerTeamVisibilityComponent;
import com.desertkun.brainout.data.WayPointMap;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.BotControllerComponentData;
import com.desertkun.brainout.data.components.ServerFlagComponentData;
import com.desertkun.brainout.data.components.ServerTeamVisibilityComponentData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.utils.RandomValue;

public class TaskAssaultAFlag extends Task
{
    private Vector2 tmp;
    private final FlagData flagData;
    private float timer;
    private boolean reachedFlagData;
    private float changeAngleCounter;

    public TaskAssaultAFlag(TaskStack stack, FlagData flagData)
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

        if (flagData.getTeam() == getPlayerData().getTeam() && flagData.getState() == FlagData.State.normal)
        {
            setState(Player.State.normal);
            pop();
            return;
        }

        if (checkEnemy())
            return;

        ServerFlagComponentData fg = flagData.getComponent(ServerFlagComponentData.class);

        // we have reached the flag, now we just have to hold on point
        if (reachedFlagData && Vector2.dst2(flagData.getX(), flagData.getY(),
                getPlayerData().getX(), getPlayerData().getY()) <
                fg.getTakingDistance() * fg.getTakingDistance())
        {
            holdAPoint(0.1f);
            return;
        }

        if (checkExistingFollowing())
            return;

        followFlagData();
    }

    private void holdAPoint(float dt)
    {
        setAim(true);
        setState(Player.State.sit);

        changeAngleCounter -= dt;
        if (changeAngleCounter < 0)
        {
            changeAngleCounter = MathUtils.random(0.25f, 1.0f);

            tmp.set(flagData.getX(), flagData.getY()).sub(getPlayerData().getX(), getPlayerData().getY());

            float angle = MathUtils.random(-20.f, 20.f);
            if (MathUtils.randomBoolean())
                angle += 180;

            getPlayerData().setAngle(angle);
        }
    }

    private void followFlagData()
    {
        getController().follow(flagData,
            () -> {
            reachedFlagData = true;
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
        return getController().isFollowing(flagData);
    }

    private boolean checkEnemy()
    {
        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        ServerFlagComponentData fg = flagData.getComponent(ServerFlagComponentData.class);
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

                ServerTeamVisibilityComponentData tm = activeData.getComponent(ServerTeamVisibilityComponentData.class);

                if (tm != null && !tm.isVisibleTo(getPlayerData().getOwnerId()))
                {
                    return false;
                }
            }
            else
            {
                if (!gameMode.isEnemies(activeData.getTeam(), getPlayerData().getTeam()))
                    return false;

                if (!getController().checkVisibility(activeData, maxDistance, null))
                    return false;
            }

            // only take into account those who are stealing the current flag
            // and currently not visible

            if (Vector2.dst2(
                activeData.getX(), activeData.getY(),
                getPlayerData().getX(), getPlayerData().getY()
            ) > fg.getTakingDistance() * fg.getTakingDistance())
            {
                if (!getController().checkVisibility(activeData, maxDistance, null))
                    return false;
            }

            return true;
        });

        if (a.size > 0)
        {
            pushTask(new TaskShootTarget(getStack(), a.random(),
                new RandomValue(0.5f, 1.0f), new RandomValue(0.05f, 0.1f)));

            return true;
        }

        return false;
    }
}
