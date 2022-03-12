package com.desertkun.brainout.bot.freeplay;

import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskStack;

public class TaskZombie extends Task
{
    public TaskZombie(TaskStack stack)
    {
        super(stack);
    }

    @Override
    protected void update(float dt)
    {
        pushTask(new HuntAroundWithKnife(getStack()));
    }
}
