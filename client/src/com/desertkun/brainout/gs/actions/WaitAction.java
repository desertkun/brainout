package com.desertkun.brainout.gs.actions;

public class WaitAction extends MenuAction
{
    protected float time;

    public WaitAction(float time)
    {
        this.time = time;
    }

    public WaitAction()
    {

    }

    @Override
    public void run()
    {
        //
    }

    public void skip()
    {
        this.time = 0;
    }

    @Override
    public void update(float dt)
    {
        time -= dt;
        if (time <= 0)
        {
            done();
        }
    }
}
