package com.desertkun.brainout.bot.freeplay;

import com.desertkun.brainout.BrainOutServer;
import com.desertkun.brainout.bot.Task;
import com.desertkun.brainout.bot.TaskStack;
import com.desertkun.brainout.client.Client;
import com.desertkun.brainout.components.PlayerOwnerComponent;
import com.desertkun.brainout.data.active.ActiveData;
import com.desertkun.brainout.events.ActivateActiveEvent;

public class TaskActivateItem extends Task
{
    private final ActiveData itemToActivate;
    private boolean tried;

    public TaskActivateItem(TaskStack stack, ActiveData itemToActivate)
    {
        super(stack);
        this.itemToActivate = itemToActivate;
        this.tried = false;
    }

    @Override
    protected void update(float dt)
    {
        if (!tried)
        {
            if (!BrainOutServer.EventMgr.sendEvent(itemToActivate, ActivateActiveEvent.obtain(
                getController().getClient(), getPlayerData(), "")))
            {
                pop();
            }

            tried = true;
        }
        else
        {
            PlayerOwnerComponent own =
                getPlayerData().getComponent(PlayerOwnerComponent.class);

            // activation complete
            if (own.isEnabled())
            {
                pop();
            }
        }
    }
}
