package com.desertkun.brainout.content.quest.task;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayItemsTakenOutEvent;
import com.desertkun.brainout.events.FreePlayMinuteSpent;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.SpendTime")
public class ServerSpendTime extends SpendTime implements ServerTask
{
    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayMinuteSpent:
            {
                FreePlayMinuteSpent ev = ((FreePlayMinuteSpent) event);
                ServerTask.Trigger(this, ev.player, 1);
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
