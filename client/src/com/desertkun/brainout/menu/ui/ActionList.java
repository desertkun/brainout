package com.desertkun.brainout.menu.ui;

import com.desertkun.brainout.gs.actions.MenuAction;

import java.util.LinkedList;

public class ActionList
{
    private LinkedList<MenuAction> actions;
    private MenuAction currentAction;

    public ActionList()
    {
        actions = new LinkedList<MenuAction>();
        currentAction = null;
    }

    public ActionList addAction(MenuAction menuAction)
    {
        actions.add(menuAction);

        return this;
    }

    public ActionList addFirstAction(MenuAction menuAction)
    {
        actions.addFirst(menuAction);

        return this;
    }

    public void clearActions()
    {
        actions.clear();
        currentAction = null;
    }

    public void processActions(float dt)
    {
        if (currentAction == null)
        {
            if (!actions.isEmpty())
            {
                currentAction = actions.pollFirst();
                currentAction.run();
            }
        }
        else
        {
            if (currentAction.isDone())
            {
                currentAction = null;
                processActions(dt);
            }
            else
            {
                currentAction.update(dt);
            }
        }
    }

    public MenuAction getCurrentAction()
    {
        return currentAction;
    }
}
