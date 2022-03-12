package com.desertkun.brainout.bot;

import com.badlogic.gdx.math.MathUtils;
import com.desertkun.brainout.Constants;
import com.desertkun.brainout.data.Map;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.active.ItemData;

public class TaskFlyTo extends Task
{
    private final ActiveData flyTo;
    private final boolean interrupt;
    private float followFor;

    public TaskFlyTo(TaskStack stack, ActiveData flyTo, boolean interrupt)
    {
        super(stack);

        this.flyTo = flyTo;
        followFor = MathUtils.random(5.0f, 10.0f);
        this.interrupt = interrupt;
    }

    @Override
    protected void update(float dt)
    {
        if (interrupt)
        {
            followFor -= dt;

            if (followFor < 0)
            {
                followFor = MathUtils.random(5.0f, 10.0f);
                switch (MathUtils.random(0, 1))
                {
                    case 0:
                    {
                        getController().stopFollowing();
                        pushTask(new TaskWait(getStack(), MathUtils.random(2.0f, 5.0f)));
                        break;
                    }
                    case 1:
                    {
                        getController().stopFollowing();
                        pop();
                        break;
                    }
                }
            }
        }

        if (getController().isFollowing(flyTo))
            return;

        getController().followDirectly(flyTo, this::done);
    }

    private void done()
    {
        getController().stopFollowing();
        pop();
    }
}
