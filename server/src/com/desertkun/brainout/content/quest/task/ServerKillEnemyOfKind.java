package com.desertkun.brainout.content.quest.task;

import com.desertkun.brainout.client.PlayerClient;
import com.desertkun.brainout.events.Event;
import com.desertkun.brainout.events.FreePlayEnemyOfKindKilledEvent;
import com.desertkun.brainout.mode.ServerFreeRealization;
import com.desertkun.brainout.reflection.Reflect;
import com.desertkun.brainout.reflection.ReflectAlias;

@Reflect("content.quest.task.KillEnemyOfKind")
public class ServerKillEnemyOfKind extends KillEnemyOfKind implements ServerTask
{
    @Override
    public boolean onEvent(Event event)
    {
        switch (event.getID())
        {
            case freePlayEnemyOfKindKilled:
            {
                FreePlayEnemyOfKindKilledEvent ev = ((FreePlayEnemyOfKindKilledEvent) event);

                if (ev.kind.getID().equals(getPlayer()))
                {
                    ServerTask.Trigger(this, ev.player, 1);
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
