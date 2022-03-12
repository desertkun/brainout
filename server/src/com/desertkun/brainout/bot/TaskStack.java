package com.desertkun.brainout.bot;

import com.badlogic.gdx.utils.Queue;
import com.desertkun.brainout.data.components.BotControllerComponentData;

public class TaskStack
{
    private Queue<Task> tasks;
    private BotControllerComponentData controller;

    public TaskStack(BotControllerComponentData controller)
    {
        this.controller = controller;
        this.tasks = new Queue<>();
    }

    public void update(float dt)
    {
        if (tasks.size > 0)
        {
            tasks.last().update(dt);
        }
    }

    public void init(Task startingTask)
    {
        if (tasks.size != 0)
            return;

        tasks.addLast(startingTask);
    }

    public Queue<Task> getTasks()
    {
        return tasks;
    }

    public void pushTask(Task task)
    {
        tasks.addLast(task);
    }

    void popTask(Task task)
    {
        tasks.removeValue(task, true);
    }

    public BotControllerComponentData getController()
    {
        return controller;
    }

    public boolean isEmpty()
    {
        return tasks.size == 0;
    }
}
