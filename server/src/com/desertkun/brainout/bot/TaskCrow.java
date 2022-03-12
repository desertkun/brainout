package com.desertkun.brainout.bot;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.data.active.ActiveData;

public class TaskCrow extends Task
{
    private final ActiveData from;
    private final ActiveData to;

    private boolean first;

    public TaskCrow(TaskStack stack, ActiveData from, ActiveData to)
    {
        super(stack);

        this.from = from;
        this.to = to;
        this.first = false;
    }

    @Override
    protected void update(float dt)
    {
        first = !first;

        if (first)
        {
            pushTask(new TaskFlyTo(getStack(), to, true));
        }
        else
        {
            pushTask(new TaskFlyTo(getStack(), from, true));
        }

        pushTask(new TaskWait(getStack(), MathUtils.random(0.5f, 10.f)));
    }
}
