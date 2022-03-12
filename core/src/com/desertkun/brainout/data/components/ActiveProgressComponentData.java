package com.desertkun.brainout.data.components;

import com.desertkun.brainout.BrainOut;
import com.desertkun.brainout.content.components.ActiveProgressComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.data.components.base.Component;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

import java.util.Timer;
import java.util.TimerTask;

@Reflect("ActiveProgressComponent")
@ReflectAlias("data.components.ActiveProgressComponentData")
public class ActiveProgressComponentData extends Component<ActiveProgressComponent>
{
    private long startTime;
    private long endTime;
    private TimerTask task;
    private Runnable cancelled;
    private boolean cancellable;

    public ActiveProgressComponentData(ActiveData activeData,
                                       ActiveProgressComponent contentComponent)
    {
        super(activeData, contentComponent);

        startTime = 0;
        endTime = 0;
        task = null;
        cancellable = false;
    }

    public void startNonCancellable(float time, Runnable complete)
    {
        start(time, false, complete, null);
    }

    public void startCancellable(float time, Runnable complete, Runnable cancelled)
    {
        start(time, true, complete, cancelled);
    }

    private void start(float time, boolean cancellable, Runnable complete, Runnable cancelled)
    {
        if (task != null)
        {
            task.cancel();
        }

        long now = System.currentTimeMillis();
        long timeL = (long)(time * 1000.0f);

        this.startTime = now;
        this.endTime = now + timeL;

        this.cancelled = cancelled;
        this.cancellable = cancellable;
        this.task = new TimerTask()
        {
            @Override
            public void run()
            {
                BrainOut.getInstance().postRunnable(() ->
                {
                    if (!((ActiveData) getComponentObject()).isAlive())
                    {
                        if (cancelled != null)
                        {
                            cancelled.run();
                        }
                        return;
                    }

                    complete.run();

                    cleanUp();
                });
            }
        };

        BrainOut.Timer.schedule(task, timeL);

        ActiveProgressVisualComponentData visual =
            getComponentObject().getComponent(ActiveProgressVisualComponentData.class);

        if (visual != null)
        {
            visual.start(startTime, endTime, cancellable);
        }
    }

    public long getStartTime()
    {
        return startTime;
    }

    public long getEndTime()
    {
        return endTime;
    }

    public boolean cancel()
    {
        if (!isRunning())
        {
            return false;
        }

        if (!cancellable)
        {
            return false;
        }

        task.cancel();

        if (cancelled != null)
        {
            cancelled.run();
        }

        cleanUp();

        return true;
    }

    public boolean isRunning()
    {
        return task != null;
    }

    private void cleanUp()
    {
        startTime = 0;
        endTime = 0;
        task = null;
        cancellable = false;

        ActiveProgressVisualComponentData visual =
                getComponentObject().getComponent(ActiveProgressVisualComponentData.class);

        if (visual != null)
        {
            visual.stop();
        }
    }

    @Override
    public boolean hasRender()
    {
        return false;
    }

    @Override
    public boolean hasUpdate()
    {
        return false;
    }

    @Override
    public boolean onEvent(Event event)
    {
        return false;
    }
}
