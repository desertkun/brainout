package com.desertkun.brainout.bot;

import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.utils.RandomValue;

public class TaskWait extends Task
{
    private float amount;

    public TaskWait(TaskStack stack, float amount)
    {
        super(stack);

        this.amount = amount;
    }

    @Override
    public void gotShotFrom(ActiveData shooter)
    {
        pushTask(new TaskFollowAndShootTarget(getStack(), shooter,
            new RandomValue(0.5f, 1.0f), new RandomValue(0.45f, 1.0f)));
    }

    @Override
    protected void update(float dt)
    {
        amount -= dt;

        ActiveData enemy = checkForEnemies();

        if (enemy != null)
        {
            popMeAndPushTask(new TaskFollowAndShootTarget(getStack(), enemy,
                new RandomValue(0.1f, 0.6f), new RandomValue(0.4f, 0.8f)));
            return;
        }

        if (amount < 0)
            pop();
    }
}
