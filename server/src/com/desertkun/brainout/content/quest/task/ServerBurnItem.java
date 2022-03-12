package com.desertkun.brainout.content.quest.task;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayItemBurnedEvent;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.BurnItem")
public class ServerBurnItem extends BurnItem implements ServerTask
{
    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayItemBurned:
            {
                FreePlayItemBurnedEvent ev = ((FreePlayItemBurnedEvent) event);

                if (getItems().contains(ev.item, true))
                {
                    ServerTask.Trigger(this, ev.player, ev.amount);
                }
            }
        }

        return false;
    }

    @Override
    public void started(ServerFreeRealization free, PlayerClient playerClient)
    {
    }
}
