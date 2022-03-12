package com.desertkun.brainout.bot;

import com.badlogic.gdx.utils.Array;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.client.BotClient;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.FlagData;

public class TaskEnforceFlags extends Task
{
    private final BotClient client;
    private float timer;

    public TaskEnforceFlags(TaskStack stack, BotClient client)
    {
        super(stack);

        this.client = client;
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0)
            return;

        timer = 0.1f;

        getController().stopFollowing();

        switch (client.getRole())
        {
            case protect:
            {
                protect();
                break;
            }
            case assault:
            {
                assault();
                break;
            }
            case hunter:
            {
                hunter();
                break;
            }
        }
    }

    private void protect()
    {
        Array<ActiveData> a = getMap().getActivesForTag(
            Constants.ActiveTags.SPAWNABLE, activeData ->
        {
            if (!(activeData instanceof FlagData))
                return false;

            return activeData.getTeam() == getPlayerData().getTeam();
        });

        if (a.size > 0)
        {
            popMeAndPushTask(new TaskHoldAFlag(getStack(), ((FlagData) a.random())));
        }
        else
        {
            assault();
        }
    }

    private void assault()
    {
        Array<ActiveData> a = getMap().getActivesForTag(
            Constants.ActiveTags.SPAWNABLE, activeData ->
        {
            if (!(activeData instanceof FlagData))
                return false;

            if (activeData.getTeam() != getPlayerData().getTeam())
                return true;

            return ((FlagData) activeData).getState() != FlagData.State.normal;
        });

        if (a.size > 0)
        {
            popMeAndPushTask(new TaskAssaultAFlag(getStack(), ((FlagData) a.random())));
        }
        else
        {
            hunter();
        }
    }

    private void hunter()
    {
        pushTask(new TaskHunter(getStack(), client));
    }
}
