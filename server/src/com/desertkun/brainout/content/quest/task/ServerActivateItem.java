package com.desertkun.brainout.content.quest.task;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayItemActivatedEvent;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.ActivateItem")
public class ServerActivateItem extends ActivateItem implements ServerTask
{
    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayItemActivated:
            {
                FreePlayItemActivatedEvent ev = ((FreePlayItemActivatedEvent) event);

                if (getItem().equals(ev.event))
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
        //
    }
}
