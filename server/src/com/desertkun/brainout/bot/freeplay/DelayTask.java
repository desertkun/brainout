package com.desertkun.brainout.bot.freeplay;

import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskStack;

public class DelayTask extends Task
{
    private float delayFor;

    public DelayTask(TaskStack stack, float delayFor)
    {
        super(stack);
        this.delayFor = delayFor;
    }

    @Override
    protected void update(float dt)
    {
        delayFor -= dt;
        if (delayFor < 0)
        {
            pop();
        }
    }
}
