package com.desertkun.brainout.gs.actions;

public abstract class MenuAction implements Runnable
{
    private String name;
    private boolean done;

    public MenuAction()
    {
        done = false;
    }

    public void done()
    {
        done = true;
    }

    public boolean isDone()
    {
        return done;
    }

    public void update(float dt) {}

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
