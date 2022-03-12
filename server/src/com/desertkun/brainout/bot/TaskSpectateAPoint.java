package com.desertkun.brainout.bot;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.content.active.Player;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.PlayerData;
import com.desertkun.brainout.data.components.ServerTeamVisibilityComponentData;
import com.desertkun.brainout.mode.GameMode;

public class TaskSpectateAPoint extends Task
{
    private final Vector2 tmp;
    private final Vector2 watchPoint;
    private final EnemyNoticedCallback enemyNoticedCallback;
    private float time, counter, changeAngleCounter;

    public TaskSpectateAPoint(TaskStack stack, float x, float y, float time, EnemyNoticedCallback enemyNoticedCallback)
    {
        super(stack);

        this.time = time;
        this.watchPoint = new Vector2(x, y);
        this.enemyNoticedCallback = enemyNoticedCallback;
        tmp = new Vector2();
    }

    @Override
    protected void pop()
    {
        setAim(false);
        setState(Player.State.normal);

        super.pop();
    }

    @Override
    protected void update(float dt)
    {
        getController().stopFollowing();

        time -= dt;

        if (time < 0)
        {
            pop();
            return;
        }

        counter -= dt;

        if (counter < 0)
        {
            counter = 0.25f;

            setAim(true);
            setState(Player.State.sit);

            if (enemyNoticedCallback != null)
                checkEnemy();
        }

        changeAngleCounter -= dt;
        if (changeAngleCounter < 0)
        {
            changeAngleCounter = MathUtils.random(0.25f, 1.0f);

            tmp.set(watchPoint).sub(getPlayerData().getX(), getPlayerData().getY());
            float angle = tmp.angleDeg() + MathUtils.random(-10.f, 10.f);

            getPlayerData().setAngle(angle);
        }
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

                ServerTeamVisibilityComponentData cmp =
                    activeData.getComponent(ServerTeamVisibilityComponentData.class);

                if (cmp != null && cmp.isVisibleTo(getPlayerData().getOwnerId()))
                {
                    if (Vector2.dst2(
                        activeData.getX(), activeData.getY(),
                        watchPoint.x, watchPoint.y) < 16.0 * 16.0f)
                    {
                        return true;
                    }
                }
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
