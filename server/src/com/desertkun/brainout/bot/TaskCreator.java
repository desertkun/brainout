package com.desertkun.brainout.bot;

public class TaskCreator extends Task
{
    private final Creator creator;

    public interface Creator
    {
        void createNew(TaskStack stack, TaskCreator creator);
    }

    public TaskCreator(TaskStack stack, Creator creator)
    {
        super(stack);
        this.creator = creator;
    }

    @Override
    protected void update(float dt)
    {
        reloadWeapon();

        creator.createNew(getStack(), this);
    }
}
