package com.desertkun.brainout.bot;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.ServerTeamVisibilityComponentData;
import com.desertkun.brainout.utils.RandomValue;

public class TaskAssault extends Task
{
    private final BotClient client;

    public TaskAssault(TaskStack stack, BotClient client)
    {
        super(stack);

        this.client = client;
    }

    @Override
    protected void update(float dt)
    {
        int action = MathUtils.random(1, 5);

        switch (action)
        {
            // go to random point of interest
            case 1:
            case 2:
            {
                Array<ActiveData> a = getMap().getActivesForTag(
                    Constants.ActiveTags.POINT_OF_INTEREST, activeData -> true);

                if (a.size > 0)
                {
                    popMeAndPushTask(new TaskFollowTarget(getStack(), a.random(), this::enemyNoticed, true)
                    {
                        @Override
                        protected void stuck()
                        {
                            pop();
                        }
                    });
                }
                else
                {
                    popMeAndPushTask(new TaskHunter(getStack(), client));
                }

                break;
            }
            // spectate a random point of interest from the current position
            case 3:
            {
                Array<ActiveData> a = getMap().getActivesForTag(
                    Constants.ActiveTags.POINT_OF_INTEREST, target ->
                {
                    if (Vector2.dst2(getPlayerData().getX(), getPlayerData().getY(), target.getX(), target.getY())
                            < 3.0f * 3.0f)
                    {
                        return false;
                    }

                    return getController().checkVisibility(target.getX(), target.getY());
                });

                if (a.size > 0)
                {
                    ActiveData activeData = a.random();

                    popMeAndPushTask(new TaskSpectateAPoint(getStack(), activeData.getX(), activeData.getY(),
                        MathUtils.random(3.0f, 10.0f), this::enemyNoticed));
                }
                else
                {
                    popMeAndPushTask(new TaskHunter(getStack(), client));
                }

                break;
            }
            // follow to kill a visible player (aka karma)
            default:
            {
                ActiveData activeData = checkForEnemies();
                if (activeData != null)
                {
                    enemyNoticed(getStack(), activeData);
                }

                break;
            }
        }
    }

    private boolean enemyNoticed(TaskStack taskStack, ActiveData activeData)
    {
        popMeAndPushTask(new TaskFollowAndShootTarget(taskStack, activeData,
            new RandomValue(0.1f, 0.6f), new RandomValue(0.3f, 1.0f))
        {
            @Override
            protected void stuck()
            {
                pop();
            }
        });

        return true;
    }
}
