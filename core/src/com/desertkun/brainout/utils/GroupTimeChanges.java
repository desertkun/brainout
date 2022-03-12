package com.desertkun.brainout.utils;

import com.badlogic.gdx.utils.Array;

public class GroupTimeChanges extends TimedChanges
{
    private Array<TimedChanges> child;

    public GroupTimeChanges()
    {
        super("group");

        this.child = new Array<>();
    }

    @Override
    public boolean changesMade()
    {
        return false;
    }

    @Override
    public boolean update(float dt)
    {
        for (TimedChanges timedChanges: child)
        {
            if (timedChanges.update(dt))
            {
                reset(timedChanges);
                return true;
            }
        }

        return false;
    }

    @Override
    public void reset(TimedChanges who)
    {
        for (TimedChanges changes : child)
        {
            if (who != changes && !changes.canBeResetByOthers())
            {
                continue;
            }

            changes.reset(who);
        }
    }

    @Override
    public void sendChanges()
    {
        //
    }

    public Array<TimedChanges> getChild()
    {
        return child;
    }
}
