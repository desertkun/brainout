package com.desertkun.brainout.menu;

public abstract class ForceTopMenu extends Menu
{
    private boolean onTop;

    public ForceTopMenu()
    {
        onTop = true;
    }

    @Override
    public boolean stayOnTop()
    {
        return onTop;
    }

    protected void close()
    {
        onTop = false;
        pop();
    }

    @Override
    public boolean escape()
    {
        close();
        return true;
    }
}
