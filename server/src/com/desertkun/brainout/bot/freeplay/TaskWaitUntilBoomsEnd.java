package com.desertkun.brainout.bot.freeplay;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskFollowAndShootTarget;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.bot.TaskWait;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.mode.GameMode;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.utils.RandomValue;

public class TaskWaitUntilBoomsEnd extends Task
{
    private float timer;

    public TaskWaitUntilBoomsEnd(TaskStack stack)
    {
        super(stack);

        this.timer = timer;
    }

    @Override
    protected void update(float dt)
    {
        timer -= dt;

        if (timer > 0f)
            return;

        timer = 0.1f;

        ActiveData enemy = checkForEnemies();

        if (enemy != null)
        {
            popMeAndPushTask(new TaskFollowAndShootTarget(getStack(), enemy,
                new RandomValue(0.1f, 0.6f), new RandomValue(0.4f, 0.8f)));
            return;
        }

        GameMode gameMode = BrainOutServer.Controller.getGameMode();

        if (!(gameMode.getRealization() instanceof ServerFreeRealization))
        {
            pop();
            return;
        }

        ServerFreeRealization free = ((ServerFreeRealization) gameMode.getRealization());

        if (!free.areThingsExploding())
        {
            // wait just a little more
            popMeAndPushTask(new TaskWait(getStack(), 5.0f));
        }
    }
}
